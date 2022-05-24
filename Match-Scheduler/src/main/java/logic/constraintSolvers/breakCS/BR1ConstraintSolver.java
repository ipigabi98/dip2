package logic.constraintSolvers.breakCS;

import logic.constraintSolvers.ConstraintSolver;
import logic.constraints.SuperConstraint;
import logic.constraints.breakConstraints.BR1;
import logic.entities.ConstraintType;
import logic.entities.Team;
import gurobi.*;
import logic.scheduler.MatchScheduler;
import logic.wrappers.MatchDataWrapper;

import java.util.List;
import java.util.stream.Collectors;


/*
    <BR1 teams="0" intp="0" mode2="HA" slots="1" type="HARD"/>

    Each team in teams has at most intp home breaks (mode2 = "H"), away breaks (mode2 = "A"), or breaks (mode2 = "HA") during time slots in slots.

    Team 0 cannot have a break on time slot 1.
*/
public class BR1ConstraintSolver extends ConstraintSolver {

    public BR1ConstraintSolver(MatchScheduler scheduler) {
        this.scheduler = scheduler;
        this.constraints = (List<BR1>)(List<?>) this.scheduler.getBr1List();
        this.hardConstraints = (List<BR1>)(List<?>) filterListByType(constraints, ConstraintType.HARD);
        this.softConstraints = (List<BR1>)(List<?>) filterListByType(constraints, ConstraintType.SOFT);
    }

    @Override
    public GRBModel addHardConstraints(GRBModel model) throws GRBException {
        if (this.hardConstraints.isEmpty()) {
            return model;
        }

        int counter = 1;
        for (SuperConstraint sc : this.hardConstraints) {
            BR1 br1 = (BR1) sc;
            int max = br1.getIntp();
            String mode = br1.getMode2();
            int teamId = br1.getTeams();
            GRBLinExpr expr = new GRBLinExpr();

            for (int round : br1.getSlots()) {
                if (round == 0) {
                    continue;
                }

                if (mode.equals("H") || mode.equals("HA")) {
                    List<MatchDataWrapper> previousRound = findByTeamAndRound(teamId, round - 1, "home");
                    List<MatchDataWrapper> breakRound = findByTeamAndRound(teamId, round, "home");
                    for (MatchDataWrapper mdw1 : previousRound) {
                        Team homeTeam1 = mdw1.getMatch().getHomeTeam();
                        Team awayTeam1 = mdw1.getMatch().getAwayTeam();

                        for (MatchDataWrapper mdw2 : breakRound) {
                            Team awayTeam2 = mdw2.getMatch().getAwayTeam();

                            if (awayTeam1 != awayTeam2) {
                                GRBVar min = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "BR1_HARD_MIN_VAR_HOME#" + counter + "#__" + homeTeam1.getId() + "-" + awayTeam1.getId() + "__" + homeTeam1.getId() + "-" + awayTeam2.getId() + "#" + round);
                                GRBVar[] vars = new GRBVar[2];
                                vars[0] = this.scheduler.getVariableByMatch().get(mdw1);
                                vars[1] = this.scheduler.getVariableByMatch().get(mdw2);
                                model.addGenConstrAnd(min, vars, "BR1_HARD_MIN_CONST_HOME#" + counter + "#__" + homeTeam1.getId() + "-" + awayTeam1.getId() + "__" + homeTeam1.getId() + "-" + awayTeam2.getId() + "#" + round);
                                expr.addTerm(1.0, min);
                            }
                        }
                    }
                }
                if (mode.equals("A") || mode.equals("HA")) {
                    List<MatchDataWrapper> previousRound = findByTeamAndRound(teamId, round - 1, "away");
                    List<MatchDataWrapper> breakRound = findByTeamAndRound(teamId, round, "away");
                    for (MatchDataWrapper mdw1 : previousRound) {
                        Team homeTeam1 = mdw1.getMatch().getHomeTeam();
                        Team awayTeam1 = mdw1.getMatch().getAwayTeam();

                        for (MatchDataWrapper mdw2 : breakRound) {
                            Team homeTeam2 = mdw2.getMatch().getHomeTeam();

                            if (homeTeam1 != homeTeam2) {
                                GRBVar min = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "BR1_HARD_MIN_VAR_AWAY#" + counter + "#__" + homeTeam1.getId() + "-" + awayTeam1.getId() + "__" + homeTeam2.getId() + "-" + awayTeam1.getId() + "#" + round);
                                GRBVar[] vars = new GRBVar[2];
                                vars[0] = this.scheduler.getVariableByMatch().get(mdw1);
                                vars[1] = this.scheduler.getVariableByMatch().get(mdw2);
                                model.addGenConstrAnd(min, vars, "BR1_HARD_MIN_CONST_AWAY#" + counter + "#__" + homeTeam1.getId() + "-" + awayTeam1.getId() + "__" + homeTeam2.getId() + "-" + awayTeam1.getId() + "#" + round);
                                expr.addTerm(1.0, min);
                            }
                        }
                    }
                }
            }
            model.addConstr(expr, GRB.LESS_EQUAL, max, "BR1_CONST_" + counter);
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
            BR1 br1 = (BR1) sc;
            int max = br1.getIntp();
            String mode = br1.getMode2();
            int teamId = br1.getTeams();
            GRBLinExpr expr = new GRBLinExpr();

            int penalty = br1.getPenalty();

            for (int round : br1.getSlots()) {
                if (round == 0) {
                    continue;
                }

                if (mode.equals("H") || mode.equals("HA")) {
                    List<MatchDataWrapper> previousRound = findByTeamAndRound(teamId, round - 1, "home");
                    List<MatchDataWrapper> breakRound = findByTeamAndRound(teamId, round, "home");
                    for (MatchDataWrapper mdw1 : previousRound) {
                        Team homeTeam1 = mdw1.getMatch().getHomeTeam();
                        Team awayTeam1 = mdw1.getMatch().getAwayTeam();

                        for (MatchDataWrapper mdw2 : breakRound) {
                            Team awayTeam2 = mdw2.getMatch().getAwayTeam();

                            if (awayTeam1 != awayTeam2) {
                                GRBVar min = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "BR1_SOFT_MIN_VAR_HOME#" + counter + "#__" + homeTeam1.getId() + "-" + awayTeam1.getId() + "__" + homeTeam1.getId() + "-" + awayTeam2.getId() + "#" + round);
                                GRBVar[] vars = new GRBVar[2];
                                vars[0] = this.scheduler.getVariableByMatch().get(mdw1);
                                vars[1] = this.scheduler.getVariableByMatch().get(mdw2);
                                model.addGenConstrAnd(min, vars, "BR1_SOFT_MIN_CONST_HOME#" + counter + "#__" + homeTeam1.getId() + "-" + awayTeam1.getId() + "__" + homeTeam1.getId() + "-" + awayTeam2.getId() + "#" + round);
                                expr.addTerm(1.0, min);
                            }
                        }
                    }
                }
                if (mode.equals("A") || mode.equals("HA")) {
                    List<MatchDataWrapper> previousRound = findByTeamAndRound(teamId, round - 1, "away");
                    List<MatchDataWrapper> breakRound = findByTeamAndRound(teamId, round, "away");
                    for (MatchDataWrapper mdw1 : previousRound) {
                        Team homeTeam1 = mdw1.getMatch().getHomeTeam();
                        Team awayTeam1 = mdw1.getMatch().getAwayTeam();

                        for (MatchDataWrapper mdw2 : breakRound) {
                            Team homeTeam2 = mdw2.getMatch().getHomeTeam();

                            if (homeTeam1 != homeTeam2) {
                                GRBVar min = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "BR1_SOFT_MIN_VAR_AWAY#" + counter + "#__" + homeTeam1.getId() + "-" + awayTeam1.getId() + "__" + homeTeam2.getId() + "-" + awayTeam1.getId() + "#" + round);
                                GRBVar[] vars = new GRBVar[2];
                                vars[0] = this.scheduler.getVariableByMatch().get(mdw1);
                                vars[1] = this.scheduler.getVariableByMatch().get(mdw2);
                                model.addGenConstrAnd(min, vars, "BR1_SOFT_MIN_CONST_AWAY#" + counter + "#__" + homeTeam1.getId() + "-" + awayTeam1.getId() + "__" + homeTeam2.getId() + "-" + awayTeam1.getId() + "#" + round);
                                expr.addTerm(1.0, min);
                            }
                        }
                    }
                }
            }

            GRBLinExpr exprMax = null;

            if (max < Integer.MAX_VALUE) {
                exprMax = new GRBLinExpr();
                exprMax.multAdd(1.0, expr);
            }

            String name = String.valueOf(counter);
            if (exprMax != null) {
                exprMax.addConstant(-1.0 * max);
                GRBVar maxResult = getMaxResult(model, exprMax, name);
                this.scheduler.addObjectiveTerm(maxResult, penalty);
            }

            counter++;
        }
        return model;
    }

    private List<MatchDataWrapper> findByTeamAndRound(int teamId, int round, String mode) {
        if (mode.equals("home")) {
            return this.scheduler.getVariableByMatch()
                    .keySet()
                    .stream()
                    .filter(mdw -> mdw.getMatch().getHomeTeam().getId() == teamId
                            && mdw.getRound() == round
                    ).collect(Collectors.toList());
        } else {
            return this.scheduler.getVariableByMatch()
                    .keySet()
                    .stream()
                    .filter(mdw -> mdw.getMatch().getAwayTeam().getId() == teamId
                            && mdw.getRound() == round
                    ).collect(Collectors.toList());
        }
    }
}
