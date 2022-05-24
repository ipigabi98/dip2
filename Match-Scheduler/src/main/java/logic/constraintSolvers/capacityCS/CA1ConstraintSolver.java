package logic.constraintSolvers.capacityCS;

import logic.constraintSolvers.ConstraintSolver;
import logic.constraints.SuperConstraint;
import logic.constraints.capacityConstraints.CA1;
import logic.entities.ConstraintType;
import gurobi.*;
import logic.scheduler.MatchScheduler;
import logic.wrappers.MatchDataWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
    <CA1 teams="0" max="0" mode="H" slots="0" type="HARD"/>

    Each team from teams plays at most max home games (mode = "H") or away games
    (mode = "A") during time slots in slots.

    Team 0 cannot play at home on time slot 0.
* */
public class CA1ConstraintSolver extends ConstraintSolver {

    public CA1ConstraintSolver(MatchScheduler scheduler) {
        this.scheduler = scheduler;
        this.constraints = (List<CA1>)(List<?>) this.scheduler.getCa1List();
        this.hardConstraints = (List<CA1>)(List<?>) filterListByType(constraints, ConstraintType.HARD);
        this.softConstraints = (List<CA1>)(List<?>) filterListByType(constraints, ConstraintType.SOFT);
    }

    @Override
    public GRBModel addHardConstraints(GRBModel model) throws GRBException {
        if (this.hardConstraints.isEmpty()) {
            return model;
        }

        int counter = 1;
        for (SuperConstraint sc : this.hardConstraints) {
            CA1 ca1 = (CA1) sc;
            int min = ca1.getMin();
            int max = ca1.getMax();
            String mode = ca1.getMode();
            int teamId = Integer.parseInt(ca1.getTeams());
            List<Integer> rounds = ca1.getSlots();

            GRBLinExpr expr = new GRBLinExpr();

            List<MatchDataWrapper> sortedList = filterMatchByTeamAndPlace(teamId, rounds, mode);
            for (MatchDataWrapper mdw : sortedList) {
                expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
            }

            model.addConstr(expr, GRB.LESS_EQUAL, max, "CA1_HARD_MAX_" + counter);
            model.addConstr(expr, GRB.GREATER_EQUAL, min, "CA1_HARD_MIN_" + counter);

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
            CA1 ca1 = (CA1) sc;
            int min = ca1.getMin();
            int max = ca1.getMax();
            String mode = ca1.getMode();
            int penalty = ca1.getPenalty();
            int teamId = Integer.parseInt(ca1.getTeams());
            List<Integer> rounds = ca1.getSlots();

            GRBLinExpr expr = new GRBLinExpr();
            List<MatchDataWrapper> sortedList = filterMatchByTeamAndPlace(teamId, rounds, mode);
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
                GRBVar minResult = getMinResult(model, exprMin, name);
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

    private List<MatchDataWrapper> filterMatchByTeamAndPlace(int teamId, List<Integer> rounds, String mode) {
        if (mode.equals("H")) {
            return this.scheduler.getVariableByMatch().keySet()
                    .stream()
                    .filter(mdw ->
                            mdw.getMatch().getHomeTeam().getId() == teamId
                            && rounds.contains(mdw.getRound())
                    ).collect(Collectors.toList());
        } else if (mode.equals("A")) {
            return this.scheduler.getVariableByMatch().keySet()
                    .stream()
                    .filter(mdw ->
                            mdw.getMatch().getAwayTeam().getId() == teamId
                            && rounds.contains(mdw.getRound())
                    ).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


}
