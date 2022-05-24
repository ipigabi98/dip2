package logic.constraintSolvers.capacityCS;

import logic.constraintSolvers.ConstraintSolver;
import logic.constraints.SuperConstraint;
import logic.constraints.capacityConstraints.CA4;
import logic.entities.ConstraintType;
import gurobi.*;
import logic.scheduler.MatchScheduler;
import logic.wrappers.MatchDataWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
    <CA4 teams1="0;1" max="3" mode1="H" teams2="2,3" mode2="GLOBAL" slots ="0;1" type="HARD"/>

    Teams in teams1 play at most max home games (mode1 = "H"), away games (mode1 = "A"), or games (mode1 = "HA") against teams in teams2 during time slots in slots
    (mode2 = "GLOBAL") or during each time slot in slots (mode2 = "EVERY").

    Teams 0 and 1 together play at most three home games against teams 2 and 3 during the first two time slots.
 */
public class CA4ConstraintSolver extends ConstraintSolver {

    public CA4ConstraintSolver(MatchScheduler scheduler) {
        this.scheduler = scheduler;
        this.constraints = (List<CA4>)(List<?>) this.scheduler.getCa4List();
        this.hardConstraints = (List<CA4>)(List<?>) filterListByType(constraints, ConstraintType.HARD);
        this.softConstraints = (List<CA4>)(List<?>) filterListByType(constraints, ConstraintType.SOFT);
    }

    @Override
    public GRBModel addHardConstraints(GRBModel model) throws GRBException {
        if (this.hardConstraints.isEmpty()) {
            return model;
        }

        int counter = 1;
        for (SuperConstraint sc : this.hardConstraints) {
            CA4 ca4 = (CA4) sc;
            int min = ca4.getMin();
            int max = ca4.getMax();
            String mode1 = ca4.getMode1();
            String mode2 = ca4.getMode2();

            if (mode2.equals("EVERY")) {
                for (int round : ca4.getSlots()) {
                    List<Integer> rounds = new ArrayList<>();
                    rounds.add(round);
                    GRBLinExpr expr = new GRBLinExpr();

                    if (mode1.equals("H")) {
                        List<MatchDataWrapper> sortedList = filterMatchesByHomeTeamByAwayTeamByRound(ca4.getTeams1(), ca4.getTeams2(), rounds, "H");
                        for (MatchDataWrapper mdw : sortedList) {
                            expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                        }
                    }

                    if (mode1.equals("A")) {
                        List<MatchDataWrapper> sortedList = filterMatchesByHomeTeamByAwayTeamByRound(ca4.getTeams1(), ca4.getTeams2(), rounds, "A");
                        for (MatchDataWrapper mdw : sortedList) {
                            expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                        }
                    }

                    if (mode1.equals("HA")) {
                        List<MatchDataWrapper> sortedList = filterMatchesByHomeTeamByAwayTeamByRound(ca4.getTeams1(), ca4.getTeams2(), rounds, "HA");
                        for (MatchDataWrapper mdw : sortedList) {
                            expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                        }
                    }

                    model.addConstr(expr, GRB.GREATER_EQUAL, min,  "CA4_HARD_MIN_EVERY_" + counter + "_" + round);
                    model.addConstr(expr, GRB.LESS_EQUAL, max,  "CA4_HARD_MAX_EVERY_" + counter + "_" + round);
                }
            }

            if (mode2.equals("GLOBAL")) {
                GRBLinExpr expr = new GRBLinExpr();

                if (mode1.equals("H")) {
                    List<MatchDataWrapper> sortedList = filterMatchesByHomeTeamByAwayTeamByRound(ca4.getTeams1(), ca4.getTeams2(), ca4.getSlots(), "H");
                    for (MatchDataWrapper mdw : sortedList) {
                        expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                    }
                }

                if (mode1.equals("A")) {
                    List<MatchDataWrapper> sortedList = filterMatchesByHomeTeamByAwayTeamByRound(ca4.getTeams1(), ca4.getTeams2(), ca4.getSlots(), "A");
                    for (MatchDataWrapper mdw : sortedList) {
                        expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                    }
                }

                if (mode1.equals("HA")) {
                    List<MatchDataWrapper> sortedList = filterMatchesByHomeTeamByAwayTeamByRound(ca4.getTeams1(), ca4.getTeams2(), ca4.getSlots(), "HA");
                    for (MatchDataWrapper mdw : sortedList) {
                        expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                    }
                }

                model.addConstr(expr, GRB.GREATER_EQUAL, min,  "CA4_HARD_MIN_GLOBAL_" + counter);
                model.addConstr(expr, GRB.LESS_EQUAL, max,  "CA4_HARD_MAX_GLOBAL_" + counter);

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
            CA4 ca4 = (CA4) sc;
            int min = ca4.getMin();
            int max = ca4.getMax();
            int penalty = ca4.getPenalty();
            String mode1 = ca4.getMode1();
            String mode2 = ca4.getMode2();

            if (mode2.equals("EVERY")) {
                for (int round : ca4.getSlots()) {
                    List<Integer> rounds = new ArrayList<>();
                    rounds.add(round);
                    GRBLinExpr expr = new GRBLinExpr();
                    /*GRBVar indMin = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "INDICATOR_MIN_CA4_EVERY_" + counter + "_" + round);
                    GRBVar indMax = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "INDICATOR_MAX_CA4_EVERY_" + counter + "_" + round);*/

                    if (mode1.equals("H")) {
                        List<MatchDataWrapper> sortedList = filterMatchesByHomeTeamByAwayTeamByRound(ca4.getTeams1(), ca4.getTeams2(), rounds, "H");
                        for (MatchDataWrapper mdw : sortedList) {
                            expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                        }
                    }

                    if (mode1.equals("A")) {
                        List<MatchDataWrapper> sortedList = filterMatchesByHomeTeamByAwayTeamByRound(ca4.getTeams1(), ca4.getTeams2(), rounds, "A");
                        for (MatchDataWrapper mdw : sortedList) {
                            expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                        }
                    }

                    if (mode1.equals("HA")) {
                        List<MatchDataWrapper> sortedList = filterMatchesByHomeTeamByAwayTeamByRound(ca4.getTeams1(), ca4.getTeams2(), rounds, "HA");
                        for (MatchDataWrapper mdw : sortedList) {
                            expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                        }
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

                    String name = String.valueOf(counter + "_" + round);
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

                    /*model.addGenConstrIndicator(indMin, 0, expr, GRB.GREATER_EQUAL, min,  "CA4_SOFT_MIN_EVERY_" + counter + "_" + round);
                    model.addGenConstrIndicator(indMax, 0, expr, GRB.LESS_EQUAL, max,  "CA4_SOFT_MAX_EVERY_" + counter + "_" + round);

                    this.logic.scheduler.addObjectiveTerm(indMin, penalty);
                    this.logic.scheduler.addObjectiveTerm(indMax, penalty);*/
                }
            }

            if (mode2.equals("GLOBAL")) {
                GRBLinExpr expr = new GRBLinExpr();
                /*GRBVar indMin = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "INDICATOR_MIN_CA4_GLOBAL_" + counter);
                GRBVar indMax = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "INDICATOR_MAX_CA4_GLOBAL_" + counter);*/

                if (mode1.equals("H")) {
                    List<MatchDataWrapper> sortedList = filterMatchesByHomeTeamByAwayTeamByRound(ca4.getTeams1(), ca4.getTeams2(), ca4.getSlots(), "H");
                    for (MatchDataWrapper mdw : sortedList) {
                        expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                    }
                }

                if (mode1.equals("A")) {
                    List<MatchDataWrapper> sortedList = filterMatchesByHomeTeamByAwayTeamByRound(ca4.getTeams1(), ca4.getTeams2(), ca4.getSlots(), "A");
                    for (MatchDataWrapper mdw : sortedList) {
                        expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                    }
                }

                if (mode1.equals("HA")) {
                    List<MatchDataWrapper> sortedList = filterMatchesByHomeTeamByAwayTeamByRound(ca4.getTeams1(), ca4.getTeams2(), ca4.getSlots(), "HA");
                    for (MatchDataWrapper mdw : sortedList) {
                        expr.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                    }
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

                /*model.addGenConstrIndicator(indMin, 0, expr, GRB.GREATER_EQUAL, min,  "CA4_SOFT_MIN_GLOBAL_" + counter);
                model.addGenConstrIndicator(indMax, 0, expr, GRB.LESS_EQUAL, max,  "CA4_SOFT_MAX_GLOBAL_" + counter);

                this.logic.scheduler.addObjectiveTerm(indMin, penalty);
                this.logic.scheduler.addObjectiveTerm(indMax, penalty);*/
            }
            counter++;
        }

        return model;
    }

    private List<MatchDataWrapper> filterMatchesByHomeTeamByAwayTeamByRound(List<Integer> team1Ids, List<Integer> team2Ids, List<Integer> rounds, String mode) {
        List<MatchDataWrapper> result = new ArrayList<>();

        if (mode.equals("H")) {
            result = this.scheduler.getVariableByMatch().keySet()
                    .stream()
                    .filter(mdw -> team1Ids.contains(mdw.getMatch().getHomeTeam().getId())
                            && team2Ids.contains(mdw.getMatch().getAwayTeam().getId())
                            && rounds.contains(mdw.getRound())
                    ).collect(Collectors.toList());
        }

        if (mode.equals("A")) {
            result = this.scheduler.getVariableByMatch().keySet()
                    .stream()
                    .filter(mdw -> team1Ids.contains(mdw.getMatch().getAwayTeam().getId())
                            && team2Ids.contains(mdw.getMatch().getHomeTeam().getId())
                            && rounds.contains(mdw.getRound())
                    ).collect(Collectors.toList());
        }

        if (mode.equals("HA")) {
            result = this.scheduler.getVariableByMatch().keySet()
                    .stream()
                    .filter(mdw -> ((team1Ids.contains(mdw.getMatch().getAwayTeam().getId())
                            && team2Ids.contains(mdw.getMatch().getHomeTeam().getId()))
                            || (team1Ids.contains(mdw.getMatch().getHomeTeam().getId())
                            && team2Ids.contains(mdw.getMatch().getAwayTeam().getId())))
                            && rounds.contains(mdw.getRound())
                    ).collect(Collectors.toList());
        }

        return result;
    }
}
