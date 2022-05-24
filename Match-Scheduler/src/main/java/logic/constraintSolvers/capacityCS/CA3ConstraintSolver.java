package logic.constraintSolvers.capacityCS;

import logic.constraintSolvers.ConstraintSolver;
import logic.constraints.SuperConstraint;
import logic.constraints.capacityConstraints.CA3;
import logic.entities.ConstraintType;
import gurobi.*;
import logic.scheduler.MatchScheduler;
import logic.wrappers.MatchDataWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
    <CA3 teams1="0" max="2" mode1="HA" teams2="1;2;3" intp="3" mode2= "SLOTS" type="SOFT"/>

    Each team in teams1 plays at most max home games (mode1 = "H"), away games (mode1 =
    "A"), or games (mode1 = "HA") against teams in teams2 in each sequence of intp time
    slots (mode2 = "SLOTS"; the only mode we consider).

    Team 0 plays at most two consecutive games against teams 1, 2, and 3.
*/
public class CA3ConstraintSolver extends ConstraintSolver {

    public CA3ConstraintSolver(MatchScheduler scheduler) {
        this.scheduler = scheduler;
        this.constraints = (List<CA3>)(List<?>) this.scheduler.getCa3List();
        this.hardConstraints = (List<CA3>)(List<?>) filterListByType(constraints, ConstraintType.HARD);
        this.softConstraints = (List<CA3>)(List<?>) filterListByType(constraints, ConstraintType.SOFT);
    }

    @Override
    public GRBModel addHardConstraints(GRBModel model) throws GRBException {
        if (this.hardConstraints.isEmpty()) {
            return model;
        }

        int counter = 1;
        for (SuperConstraint sc : this.hardConstraints) {
            CA3 ca3 = (CA3) sc;
            int max = ca3.getMax();
            String mode = ca3.getMode1();
            List<Integer> teams = ca3.getTeams2();

            for (int teamId : ca3.getTeams1()) {
                for (int round = 0; round < this.scheduler.getSlots().size() - max; round++) {
                    List<MatchDataWrapper> sortedList = filterByModeAndRoundAndTeam(teamId, teams, mode, round, max);

                    GRBLinExpr expr = new GRBLinExpr();

                    for (MatchDataWrapper mdw : sortedList) {
                        expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                    }
                    model.addConstr(expr, GRB.LESS_EQUAL, max, "CA3_HARD_" + counter + "_team_" + teamId + "_round_" + round);
                }
            }
            counter++;
        }
        return model;
    }

    @Override
    public GRBModel addSoftConstraints(GRBModel model) throws GRBException {
        if (this.softConstraints.isEmpty()) {
            return model;
        }

        int counter = 1;
        for (SuperConstraint sc : this.softConstraints) {
            CA3 ca3 = (CA3) sc;
            int max = ca3.getMax();
            int intp = ca3.getIntp();
            int penalty = ca3.getPenalty();
            String mode = ca3.getMode1();
            List<Integer> teams = ca3.getTeams2();

            for (int teamId : ca3.getTeams1()) {
                for (int round = 0; round < this.scheduler.getSlots().size() - intp + 1; round++) {
                    List<MatchDataWrapper> sortedList = filterByModeAndRoundAndTeam(teamId, teams, mode, round, intp-1);

                    GRBLinExpr expr = new GRBLinExpr();
                    for (MatchDataWrapper mdw : sortedList) {
                        expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                    }
                    expr.addConstant(-1.0 * max);

                    String name = round + "-" + (round+intp-1) + "#" + teamId + "#" + counter;
                    GRBVar maxResult = getMaxResult(model, expr, name);

                    this.scheduler.addObjectiveTerm(maxResult, penalty);
                }
            }
            counter++;
        }
        return model;
    }

    private List<MatchDataWrapper> filterByModeAndRoundAndTeam(int teamId, List<Integer> teams, String mode, int round, int max) {
        if (mode.equals("H")) {
            return this.scheduler.getVariableByMatch().keySet()
                    .stream()
                    .filter(mdw ->
                            mdw.getMatch().getHomeTeam().getId() == teamId
                            && teams.contains(mdw.getMatch().getAwayTeam().getId())
                            && mdw.getRound() >= (round)
                            && mdw.getRound() <= (round+max)
                    )
                    .collect(Collectors.toList());
        }
        if (mode.equals("A")) {
            return this.scheduler.getVariableByMatch().keySet()
                    .stream()
                    .filter(mdw ->
                            mdw.getMatch().getAwayTeam().getId() == teamId
                            && teams.contains(mdw.getMatch().getHomeTeam().getId())
                            && mdw.getRound() >= (round)
                            && mdw.getRound() <= (round+max)
                    )
                    .collect(Collectors.toList());
        }
        if (mode.equals("HA")) {
            return this.scheduler.getVariableByMatch().keySet()
                    .stream()
                    .filter(mdw ->
                            ((mdw.getMatch().getAwayTeam().getId() == teamId
                            && teams.contains(mdw.getMatch().getHomeTeam().getId()))
                            || (mdw.getMatch().getHomeTeam().getId() == teamId
                            && teams.contains(mdw.getMatch().getAwayTeam().getId())))
                            && mdw.getRound() >= (round)
                            && mdw.getRound() <= (round+max)
                    )
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
