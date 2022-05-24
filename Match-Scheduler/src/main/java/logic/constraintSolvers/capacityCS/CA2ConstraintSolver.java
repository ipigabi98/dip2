package logic.constraintSolvers.capacityCS;

import logic.constraintSolvers.ConstraintSolver;
import logic.constraints.SuperConstraint;
import logic.constraints.capacityConstraints.CA2;
import logic.entities.ConstraintType;
import gurobi.*;
import logic.scheduler.MatchScheduler;
import logic.wrappers.MatchDataWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
    <CA2 teams1="0" min="0" max="1" mode1="HA" mode2="GLOBAL" teams2="1;2" slots ="0;1;2" type="SOFT"/>

    Each team in teams1 plays at most max home games (mode1 = "H"), away games (mode1 =
    "A"), or games (mode1 = "HA") against teams (mode2 = "GLOBAL"; the only mode we
    consider) in teams2 during time slots in slots.

    Team 0 plays at most one game against teams 1 and 2 during the first three time slots.
*/
public class CA2ConstraintSolver extends ConstraintSolver {

    public CA2ConstraintSolver(MatchScheduler scheduler) {
        this.scheduler = scheduler;
        this.constraints = (List<CA2>)(List<?>) this.scheduler.getCa2List();
        this.hardConstraints = (List<CA2>)(List<?>) filterListByType(constraints, ConstraintType.HARD);
        this.softConstraints = (List<CA2>)(List<?>) filterListByType(constraints, ConstraintType.SOFT);
    }

    @Override
    public GRBModel addHardConstraints(GRBModel model) throws GRBException {
        if (this.hardConstraints.isEmpty()) {
            return model;
        }

        int counter = 1;
        for (SuperConstraint sc : this.hardConstraints) {
            CA2 ca2 = (CA2) sc;
            String mode = ca2.getMode1();
            int min = ca2.getMin();
            int max = ca2.getMax();
            int teamId = Integer.parseInt(ca2.getTeams1());

            GRBLinExpr expr = new GRBLinExpr();

            List<MatchDataWrapper> sortedList = findByModeAndOpponentAndRound(teamId, mode, ca2.getTeams2(), ca2.getSlots());
            for (MatchDataWrapper mdw : sortedList) {
                expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
            }

            model.addConstr(expr, GRB.GREATER_EQUAL, min, "CA2_HARD_MIN_" + counter);
            model.addConstr(expr, GRB.LESS_EQUAL, max, "CA2_HARD_MAX_" + counter);

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
            CA2 ca2 = (CA2) sc;
            String mode = ca2.getMode1();
            int min = ca2.getMin();
            int max = ca2.getMax();
            int penalty = ca2.getPenalty();
            int teamId = Integer.parseInt(ca2.getTeams1());

            GRBLinExpr expr = new GRBLinExpr();
            List<MatchDataWrapper> sortedList = findByModeAndOpponentAndRound(teamId, mode, ca2.getTeams2(), ca2.getSlots());
            for (MatchDataWrapper mdw : sortedList) {
                expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
            }

            GRBLinExpr exprMin = null;
            GRBLinExpr exprMax = null;

            if (min > 0) {
                exprMin = new GRBLinExpr();
                exprMin.multAdd(-1.0, expr);
            }

            if (max < Integer.MAX_VALUE) {
                exprMax = new GRBLinExpr();
                exprMax.multAdd(1.0, expr);
            }

            String name = String.valueOf(counter);
            if (exprMin != null) {
                exprMin.addConstant(1.0 * min);
                GRBVar minResult = getMaxResult(model, exprMin, name);
                this.scheduler.addObjectiveTerm(minResult, penalty);
            }

            if (exprMax != null) {
                exprMax.addConstant(-1.0 * max);
                GRBVar maxResult = getMaxResult(model, exprMax, name);
                this.scheduler.addObjectiveTerm(maxResult, penalty);
            }
            counter++;
        }

        return model;
    }

    private List<MatchDataWrapper> findByModeAndOpponentAndRound(int teamId, String mode, List<Integer> teamIds, List<Integer> rounds) {
        if (mode.equals("H")) {
            return this.scheduler.getVariableByMatch().keySet()
                    .stream()
                    .filter(mdw -> mdw.getMatch().getHomeTeam().getId() == teamId
                            && teamIds.contains(mdw.getMatch().getAwayTeam().getId())
                            && rounds.contains(mdw.getRound())
                    ).collect(Collectors.toList());
        }

        if (mode.equals("A")) {
            return this.scheduler.getVariableByMatch().keySet()
                    .stream()
                    .filter(mdw -> mdw.getMatch().getAwayTeam().getId() == teamId
                            && teamIds.contains(mdw.getMatch().getHomeTeam().getId())
                            && rounds.contains(mdw.getRound())
                    ).collect(Collectors.toList());
        }

        if (mode.equals("HA")) {
            return this.scheduler.getVariableByMatch().keySet()
                    .stream()
                    .filter(mdw -> ((mdw.getMatch().getAwayTeam().getId() == teamId
                            && teamIds.contains(mdw.getMatch().getHomeTeam().getId()))
                            || (mdw.getMatch().getHomeTeam().getId() == teamId
                            && teamIds.contains(mdw.getMatch().getAwayTeam().getId())))
                            && rounds.contains(mdw.getRound())
                    ).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
