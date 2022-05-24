package logic.constraintSolvers.breakCS;

import logic.constraintSolvers.ConstraintSolver;
import logic.constraints.SuperConstraint;
import logic.constraints.breakConstraints.BR2;
import logic.entities.ConstraintType;
import logic.entities.Team;
import gurobi.*;
import logic.scheduler.MatchScheduler;
import logic.wrappers.MatchDataWrapper;
import logic.wrappers.ObjectiveMode;
import logic.wrappers.ObjectiveWrapper;

import java.util.List;
import java.util.stream.Collectors;

/*
    <BR2 homeMode="HA" teams="0;1" mode2="LEQ" intp="2" slots="0;1;2;3" type="HARD"/>

    The sum over all breaks (homeMode = "HA", the only mode we consider) in teams is no
    more than (mode2 = "LEQ", the only mode we consider) intp during time slots in slots.
    
    Team 0 and 1 together do not have more than two breaks during the first four time slots.
*/
public class BR2ConstraintSolver extends ConstraintSolver {

    public BR2ConstraintSolver(MatchScheduler scheduler) {
        this.scheduler = scheduler;
        this.constraints = (List<BR2>)(List<?>) scheduler.getBr2List();
        this.hardConstraints = (List<BR2>)(List<?>) filterListByType(constraints, ConstraintType.HARD);
        this.softConstraints = (List<BR2>)(List<?>) filterListByType(constraints, ConstraintType.SOFT);
    }

    @Override
    public GRBModel addHardConstraints(GRBModel model) throws GRBException {
        if (this.hardConstraints.isEmpty()) {
            return model;
        }

        int counter = 1;
        for (SuperConstraint sc : this.hardConstraints) {
            BR2 br2 = (BR2) sc;
            GRBLinExpr expr = new GRBLinExpr();
            for (Integer team1Id : br2.getTeams()) {
                for (int round = 0; round < this.scheduler.getSlots().size()-1; round++) {

                    if (!br2.getSlots().contains(round+1)) {
                        continue;
                    }

                    //HOME BREAKS
                    List<MatchDataWrapper> listHome1 = filterListByRoundAndTeam(round, team1Id, "H");
                    List<MatchDataWrapper> listHome2 = filterListByRoundAndTeam((round+1), team1Id, "H");
                    for (MatchDataWrapper mdw1 : listHome1) {
                        Team homeTeam1 = mdw1.getMatch().getHomeTeam();
                        Team awayTeam1 = mdw1.getMatch().getAwayTeam();

                        for (MatchDataWrapper mdw2 : listHome2) {
                            Team awayTeam2 = mdw2.getMatch().getAwayTeam();

                            if (awayTeam1 != awayTeam2) {
                                GRBVar min = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "BR2_HARD_MIN_VAR_HOME#" + counter + "#" + homeTeam1.getId() + "-" + awayTeam2.getId() + "#" + round + "-" + (round+1));
                                GRBVar[] vars = new GRBVar[2];
                                vars[0] = this.scheduler.getVariableByMatch().get(mdw1);
                                vars[1] = this.scheduler.getVariableByMatch().get(mdw2);
                                model.addGenConstrAnd(min, vars, "BR2_HARD_MIN_CONST_HOME#" + counter + "#" + homeTeam1.getId() + "-" + awayTeam2.getId() + "#" + round + "-" + (round+1));
                                expr.addTerm(1.0, min);
                            }
                        }
                    }

                    //AWAY BREAKS
                    List<MatchDataWrapper> listAway1 = filterListByRoundAndTeam(round, team1Id, "A");
                    List<MatchDataWrapper> listAway2 = filterListByRoundAndTeam((round+1), team1Id, "A");
                    for (MatchDataWrapper mdw1 : listAway1) {
                        Team homeTeam1 = mdw1.getMatch().getHomeTeam();
                        Team awayTeam1 = mdw1.getMatch().getAwayTeam();

                        for (MatchDataWrapper mdw2 : listAway2) {
                            Team homeTeam2 = mdw2.getMatch().getHomeTeam();

                            if (homeTeam1 != homeTeam2) {
                                GRBVar min = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "BR2_HARD_MIN_VAR_AWAY#" + counter + "#" + homeTeam2.getId() + "-" + awayTeam1.getId() + "#" + round + "-" + (round+1));
                                GRBVar[] vars = new GRBVar[2];
                                vars[0] = this.scheduler.getVariableByMatch().get(mdw1);
                                vars[1] = this.scheduler.getVariableByMatch().get(mdw2);
                                model.addGenConstrAnd(min, vars, "BR2_HARD_MIN_CONST_AWAY#" + counter + "#" + homeTeam2.getId() + "-" + awayTeam1.getId() + "#" + round + "-" + (round+1));
                                expr.addTerm(1.0, min);
                            }
                        }
                    }

                }
            }
            model.addConstr(expr, GRB.LESS_EQUAL, br2.getIntp(), "BR2_CONST_" + counter);
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
            BR2 br2 = (BR2) sc;
            GRBLinExpr expr = new GRBLinExpr();
            int penalty = br2.getPenalty();

            for (Integer team1Id : br2.getTeams()) {
                for (int round = 0; round < this.scheduler.getSlots().size()-1; round++) {

                    if (!br2.getSlots().contains(round+1)) {
                        continue;
                    }

                    //HOME BREAKS
                    List<MatchDataWrapper> listHome1 = filterListByRoundAndTeam(round, team1Id, "H");
                    List<MatchDataWrapper> listHome2 = filterListByRoundAndTeam((round+1), team1Id, "H");
                    for (MatchDataWrapper mdw1 : listHome1) {
                        Team homeTeam1 = mdw1.getMatch().getHomeTeam();
                        Team awayTeam1 = mdw1.getMatch().getAwayTeam();

                        for (MatchDataWrapper mdw2 : listHome2) {
                            Team awayTeam2 = mdw2.getMatch().getAwayTeam();

                            if (awayTeam1 != awayTeam2) {
                                GRBVar min = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "BR2_SOFT_MIN_VAR_HOME#" + counter + "#" + homeTeam1.getId() + "-" + awayTeam2.getId() + "#" + round + "-" + (round+1));
                                GRBVar[] vars = new GRBVar[2];
                                vars[0] = this.scheduler.getVariableByMatch().get(mdw1);
                                vars[1] = this.scheduler.getVariableByMatch().get(mdw2);
                                model.addGenConstrAnd(min, vars, "BR2_SOFT_MIN_CONST_HOME#" + counter + "#" + homeTeam1.getId() + "-" + awayTeam2.getId() + "#" + round + "-" + (round+1));
                                expr.addTerm(1.0, min);
                            }
                        }
                    }

                    //AWAY BREAKS
                    List<MatchDataWrapper> listAway1 = filterListByRoundAndTeam(round, team1Id, "A");
                    List<MatchDataWrapper> listAway2 = filterListByRoundAndTeam((round+1), team1Id, "A");
                    for (MatchDataWrapper mdw1 : listAway1) {
                        Team homeTeam1 = mdw1.getMatch().getHomeTeam();
                        Team awayTeam1 = mdw1.getMatch().getAwayTeam();

                        for (MatchDataWrapper mdw2 : listAway2) {
                            Team homeTeam2 = mdw2.getMatch().getHomeTeam();

                            if (homeTeam1 != homeTeam2) {
                                GRBVar min = model.addVar(0.0, 1.0, 1.0, GRB.BINARY, "BR2_SOFT_MIN_VAR_AWAY#" + counter + "#" + homeTeam2.getId() + "-" + awayTeam1.getId() + "#" + round + "-" + (round+1));
                                GRBVar[] vars = new GRBVar[2];
                                vars[0] = this.scheduler.getVariableByMatch().get(mdw1);
                                vars[1] = this.scheduler.getVariableByMatch().get(mdw2);
                                model.addGenConstrAnd(min, vars, "BR2_SOFT_MIN_CONST_AWAY#" + counter + "#" + homeTeam2.getId() + "-" + awayTeam1.getId() + "#" + round + "-" + (round+1));
                                expr.addTerm(1.0, min);
                            }
                        }
                    }

                }
            }

            GRBVar obj = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "OBJECTIVE_BR2__" + counter);
            model.addGenConstrIndicator(obj, 0, expr, GRB.LESS_EQUAL, br2.getIntp(), "BR2_CONST_" + counter);
            ObjectiveWrapper wrapperMax = new ObjectiveWrapper(penalty, expr, ObjectiveMode.MAX, br2.getIntp());
            this.scheduler.addObjectiveTerm(obj, wrapperMax);
            counter++;
        }

        return model;
    }

    private List<MatchDataWrapper> filterListByRoundAndTeam(int round, int teamId, String type) {
        List<MatchDataWrapper> result = this.scheduler.getVariableByMatch().keySet()
                .stream()
                .filter( matchDataWrapper ->
                        matchDataWrapper.getMatch().getIndex() == round
                ).collect(Collectors.toList());
        if (type.equals("H")) {
            result = result.stream().filter(matchDataWrapper ->
                    matchDataWrapper.getMatch().getHomeTeam().getId() == teamId
            ).collect(Collectors.toList());
        }
        if (type.equals("A")) {
            result = result.stream().filter(matchDataWrapper ->
                    matchDataWrapper.getMatch().getAwayTeam().getId() == teamId
            ).collect(Collectors.toList());
        }

        return result;
    }
}
