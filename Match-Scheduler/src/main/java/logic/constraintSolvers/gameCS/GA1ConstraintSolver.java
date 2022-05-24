package logic.constraintSolvers.gameCS;

import logic.constraintSolvers.ConstraintSolver;
import logic.constraints.SuperConstraint;
import logic.constraints.gameConstraints.GA1;
import logic.entities.ConstraintType;
import gurobi.*;
import logic.scheduler.MatchScheduler;
import logic.wrappers.MatchDataWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
    <GA1 min="0" max="0" meetings="0,1;1,2;" slots="3" type="HARD"/>

    At least min and at most max games from G = {(i1, j1), (i2, j2), . . . } take place during time
    slots in slots.

    Game (0, 1) and (1, 2) cannot take place during time slot 3.
* */
public class GA1ConstraintSolver extends ConstraintSolver {

    public GA1ConstraintSolver(MatchScheduler scheduler) {
        this.scheduler = scheduler;
        this.constraints = (List<GA1>)(List<?>) this.scheduler.getGa1List();
        this.hardConstraints = (List<GA1>)(List<?>) filterListByType(constraints, ConstraintType.HARD);
        this.softConstraints = (List<GA1>)(List<?>) filterListByType(constraints, ConstraintType.SOFT);
    }

    @Override
    public GRBModel addHardConstraints(GRBModel model) throws GRBException {
        if (this.hardConstraints.isEmpty()) {
            return model;
        }

        int counter = 0;
        for (SuperConstraint sc : this.hardConstraints) {
            GA1 ga1 = (GA1) sc;
            int min = ga1.getMin();
            int max = ga1.getMax();
            List<Integer> rounds = ga1.getSlots();
            List<MatchDataWrapper> allSortedList = new ArrayList<>();

            for (List<Integer> opponentIds : ga1.getMeetings()) {
                int homeTeamId = opponentIds.get(0);
                int awayTeamId = opponentIds.get(1);

                List<MatchDataWrapper> sortedList = filterMatchesByHomeTeamByAwayTeamByRound(homeTeamId, awayTeamId, rounds);
                allSortedList.addAll(sortedList);
            }

            GRBLinExpr expr = new GRBLinExpr();
            for (MatchDataWrapper mdw : allSortedList) {
                expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
            }

            if (min != 0) {
                model.addConstr(expr, GRB.GREATER_EQUAL, min,  "GA1_HARD_MIN_" + counter);
            }

            model.addConstr(expr, GRB.LESS_EQUAL, max,  "GA1_HARD_MAX_" + counter);

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
            GA1 ga1 = (GA1) sc;
            int min = ga1.getMin();
            int max = ga1.getMax();
            int penalty = ga1.getPenalty();
            List<Integer> rounds = ga1.getSlots();
            List<MatchDataWrapper> allSortedList = new ArrayList<>();

            for (List<Integer> opponentIds : ga1.getMeetings()) {
                int homeTeamId = opponentIds.get(0);
                int awayTeamId = opponentIds.get(1);

                List<MatchDataWrapper> sortedList = filterMatchesByHomeTeamByAwayTeamByRound(homeTeamId, awayTeamId, rounds);
                allSortedList.addAll(sortedList);
            }

            GRBLinExpr expr = new GRBLinExpr();
            for (MatchDataWrapper mdw : allSortedList) {
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

    private List<MatchDataWrapper> filterMatchesByHomeTeamByAwayTeamByRound(int homeTeamId, int awayTeamId, List<Integer> rounds) {
        return this.scheduler.getVariableByMatch()
                .keySet().stream()
                .filter(mdw ->
                        mdw.getMatch().getHomeTeam().getId() == homeTeamId
                        && mdw.getMatch().getAwayTeam().getId() == awayTeamId
                        && rounds.contains(mdw.getRound())
                ).collect(Collectors.toList());
    }
}
