package logic.constraintSolvers.fairnessCS;

import logic.constraintSolvers.ConstraintSolver;
import logic.constraints.SuperConstraint;
import logic.constraints.fairnessConstraints.FA2;
import logic.entities.ConstraintType;
import gurobi.*;
import logic.scheduler.MatchScheduler;
import logic.wrappers.MatchDataWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
    <FA2 teams="0;1;2" mode="H" intp="1" slots="0;1;2;3" type="HARD"/>

    Each pair of teams in teams has a difference in played home games (mode = "H", the only
    mode we consider) that is not larger than intp after each time slot in slots.

    The difference in home games played between the first three teams is not larger than 1 during the first four time slots.
*/
// SPLITTED!!!!!
public class FA2ConstraintSolver extends ConstraintSolver {

    public FA2ConstraintSolver(MatchScheduler scheduler) {
        this.scheduler = scheduler;
        this.constraints = (List<FA2>)(List<?>) this.scheduler.getFa2List();
        this.hardConstraints = (List<FA2>)(List<?>) filterListByType(constraints, ConstraintType.HARD);
        this.softConstraints = (List<FA2>)(List<?>) filterListByType(constraints, ConstraintType.SOFT);
    }

    @Override
    public GRBModel addHardConstraints(GRBModel model) throws GRBException {
        if (this.hardConstraints.isEmpty()) {
            return model;
        }

        for (SuperConstraint sc : this.hardConstraints) {
            FA2 fa2 = (FA2) sc;
            int max = fa2.getIntp();
            String id = fa2.getId();
            int round = fa2.getSlots().get(0);
            int team1Id = fa2.getTeams().get(0);
            int team2Id = fa2.getTeams().get(1);

            List<MatchDataWrapper> team1HomeMatchesTillRound = filterMatchesByHomeTeamAndRound(team1Id, round);
            List<MatchDataWrapper> team2HomeMatchesTillRound = filterMatchesByHomeTeamAndRound(team2Id, round);

            GRBLinExpr sum = new GRBLinExpr();

            for (MatchDataWrapper mdw : team1HomeMatchesTillRound) {
                sum.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
            }

            for (MatchDataWrapper mdw : team2HomeMatchesTillRound) {
                sum.addTerm(-1.0, this.scheduler.getVariableByMatch().get(mdw));
            }

            model.addConstr(sum, GRB.GREATER_EQUAL, -1 * max,  "FA2_HARD_NEGATIVE_MAX_" + id);
            model.addConstr(sum, GRB.LESS_EQUAL, max,  "FA2_HARD_POSITIVE_MAX_" + id);
        }

        return model;
    }

    @Override
    public GRBModel addSoftConstraints(GRBModel model) throws GRBException {
        if (this.softConstraints.isEmpty()) {
            return model;
        }

        for (SuperConstraint sc : this.softConstraints) {
            FA2 fa2 = (FA2) sc;
            int max = fa2.getIntp();
            String id = fa2.getId();
            int penalty = fa2.getPenalty();
            int round = fa2.getSlots().get(0);
            int team1Id = fa2.getTeams().get(0);
            int team2Id = fa2.getTeams().get(1);

            List<MatchDataWrapper> team1HomeMatchesTillRound = filterMatchesByHomeTeamAndRound(team1Id, round);
            List<MatchDataWrapper> team2HomeMatchesTillRound = filterMatchesByHomeTeamAndRound(team2Id, round);

            GRBLinExpr expr = new GRBLinExpr();
            /*GRBVar indMin = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "INDICATOR_NEGATIVE_MAX_FA2_" + id);
            GRBVar indMax = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "INDICATOR_POSITIVE_MAX_FA2__" + id);
*/
            for (MatchDataWrapper mdw : team1HomeMatchesTillRound) {
                expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
            }

            for (MatchDataWrapper mdw : team2HomeMatchesTillRound) {
                expr.addTerm(-1.0, this.scheduler.getVariableByMatch().get(mdw));
            }

            GRBVar diff = model.addVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0, GRB.INTEGER, "FA2_DIFF_" + id);
            model.addConstr(diff, GRB.EQUAL, expr, "FA2_DIFF_EQUATION_" + id);

            GRBVar abs = model.addVar(0.0, Double.POSITIVE_INFINITY, 1.0, GRB.INTEGER, "FA2_ABS_" + id);
            model.addGenConstrAbs(abs, diff, "FA2_ABS_EQUATION_" + id);

            GRBLinExpr absLe = new GRBLinExpr();
            absLe.addTerm(1.0, abs);

            GRBLinExpr exprMax = new GRBLinExpr();
            exprMax.multAdd(1.0, absLe);

            String name = String.valueOf(id);
            exprMax.addConstant(-1.0 * max);
            GRBVar maxResult = getMaxResult(model, exprMax, name);
            this.scheduler.addObjectiveTerm(maxResult, penalty);

            /*model.addGenConstrIndicator(indMin, 0, expr, GRB.GREATER_EQUAL, -1 * max,  "FA2_HARD_NEGATIVE_MAX_" + id);
            model.addGenConstrIndicator(indMax, 0, expr, GRB.LESS_EQUAL, max,  "FA2_HARD_POSITIVE_MAX_" + id);

            this.logic.scheduler.addObjectiveTerm(indMin, penalty);
            this.logic.scheduler.addObjectiveTerm(indMax, penalty);*/
        }

        return model;
    }

    private List<MatchDataWrapper> filterMatchesByHomeTeamAndRound(int homeTeamId, int round) {
        return this.scheduler.getVariableByMatch()
                .keySet().stream()
                .filter(mdw ->
                        mdw.getMatch().getHomeTeam().getId() == homeTeamId
                        && mdw.getRound() <= round
                ).collect(Collectors.toList());
    }

    private List<String> getAllCombinations(List<Integer> teamIds) {
        List<String> result = new ArrayList<>();
        while (!teamIds.isEmpty()) {
            int teamId = teamIds.remove(0);
            for (int id : teamIds) {
                String match = teamId + "," + id;
                result.add(match);
            }
        }
        return result;
    }
}
