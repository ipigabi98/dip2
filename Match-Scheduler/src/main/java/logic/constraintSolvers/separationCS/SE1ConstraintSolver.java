package logic.constraintSolvers.separationCS;

import logic.constraintSolvers.ConstraintSolver;
import logic.constraints.SuperConstraint;
import logic.constraints.separationConstraints.SE1;
import logic.entities.ConstraintType;
import gurobi.*;
import logic.scheduler.MatchScheduler;
import logic.wrappers.MatchDataWrapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
    <SE1 teams="0;1" min="5" mode1="SLOTS" type="HARD"/>

    Each pair of teams in teams has at least min time slots (mode1 = "SLOTS", the only mode
    we consider) between two consecutive mutual games.

    There are at least 5 time slots between the mutual games of team 0 and 1.
*/
public class SE1ConstraintSolver extends ConstraintSolver {

    public SE1ConstraintSolver(MatchScheduler scheduler) {
        this.scheduler = scheduler;
        this.constraints = (List<SE1>)(List<?>) this.scheduler.getSe1List();
        this.hardConstraints = (List<SE1>)(List<?>) filterListByType(constraints, ConstraintType.HARD);
        this.softConstraints = (List<SE1>)(List<?>) filterListByType(constraints, ConstraintType.SOFT);
    }

    @Override
    public GRBModel addHardConstraints(GRBModel model) throws GRBException {
        if (this.hardConstraints.isEmpty()) {
            return model;
        }

        int slotsSize = this.scheduler.getSlots().size();
        Map<MatchDataWrapper, GRBVar> variableByMatch = this.scheduler.getVariableByMatch();

        int counter = 1;
        for (SuperConstraint sc : this.hardConstraints) {
            SE1 se1 = (SE1) sc;
            int min = se1.getMin();
            for (int team1Id : se1.getTeams()) {
                for (int team2Id : se1.getTeams()) {
                    for (int i = 0; i < (slotsSize - min); i++) {
                        GRBLinExpr expr = new GRBLinExpr();
                        List<MatchDataWrapper> sortedList = sortListByConditions(team1Id, team2Id, i, min);
                        if (!sortedList.isEmpty()) {
                            for (MatchDataWrapper mdw : sortedList) {
                                expr.addTerm(1.0, variableByMatch.get(mdw));
                            }
                            model.addConstr(expr, GRB.LESS_EQUAL, 1.0, "SE1_HARD#" + counter + "#" + team1Id + "#" + team2Id + "#round:" + i + "-" + (i+min));
                        }
                    }
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

        int slotsSize = this.scheduler.getSlots().size();
        Map<MatchDataWrapper, GRBVar> variableByMatch = this.scheduler.getVariableByMatch();

        int counter = 1;
        for (SuperConstraint sc : this.softConstraints) {
            SE1 se1 = (SE1) sc;
            int min = se1.getMin();
            int penalty = se1.getPenalty();
            int max = 1;

            if (min >= slotsSize) {
                GRBVar var = model.addVar(1.0, 1.0, 1.0, GRB.BINARY, "OBJECTIVE_SE1_" + counter);
                this.scheduler.addObjectiveTerm(var, penalty);
                continue;
            }

            for (int team1Id : se1.getTeams()) {
                for (int team2Id : se1.getTeams()) {
                    for (int i = 0; i < (slotsSize - min); i++) {
                        List<MatchDataWrapper> sortedList = sortListByConditions(team1Id, team2Id, i, min);
                        if (!sortedList.isEmpty()) {

                            GRBLinExpr expr = new GRBLinExpr();
                            for (MatchDataWrapper mdw : sortedList) {
                                expr.addTerm(1.0, variableByMatch.get(mdw));
                            }
                            GRBVar ind = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "SE1_IND#" + counter + "#" + team1Id + "#" + team2Id + "#round" + i + "-" + (i+min));
                            /*GRBLinExpr exprMax = new GRBLinExpr();
                            exprMax.multAdd(1.0, expr);

                            String name = counter + "#" + team1Id + "#" + team2Id + "#round" + i + "-" + (i+min);

                            exprMax.addConstant(-1.0 * max);
                            GRBVar maxResult = getMaxResult(model, exprMax, name);
                            this.logic.scheduler.addObjectiveTerm(maxResult, penalty);*/

                            model.addGenConstrIndicator(ind, 0, expr, GRB.LESS_EQUAL, 1.0, "SE1_SOFT#" + counter + "#" + team1Id + "#" + team2Id + "#round" + i + "-" + (i+min));
                            this.scheduler.addObjectiveTerm(ind, penalty);
                        }
                    }
                }
            }
            counter++;
        }
        return model;
    }

    private List<MatchDataWrapper> sortListByConditions(int team1Id, int team2Id, int i, int min) {
        return this.scheduler.getVariableByMatch().keySet()
                .stream()
                .filter(mdw -> ((mdw.getMatch().getHomeTeam().getId() == team1Id
                        && mdw.getMatch().getAwayTeam().getId() == team2Id)
                        || (mdw.getMatch().getHomeTeam().getId() == team2Id
                        && mdw.getMatch().getAwayTeam().getId() == team1Id))
                        && mdw.getRound() >= i
                        && mdw.getRound() <= (i + min)
                ).collect(Collectors.toList());
    }
}
