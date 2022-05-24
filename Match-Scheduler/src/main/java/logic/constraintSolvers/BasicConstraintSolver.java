package logic.constraintSolvers;

import logic.entities.Match;
import logic.scheduler.MatchScheduler;
import logic.wrappers.MatchDataWrapper;
import logic.entities.Slot;
import logic.entities.Team;
import gurobi.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BasicConstraintSolver {
    private final Map<MatchDataWrapper, GRBVar> variableByMatch;
    private final List<Slot> slots;
    private final List<Team> teams;
    private final List<Match> matches;

    public BasicConstraintSolver(MatchScheduler scheduler) {
        this.variableByMatch = scheduler.getVariableByMatch();
        this.slots = scheduler.getSlots();
        this.teams = scheduler.getTeams();
        this.matches = scheduler.getMatches();
    }

    public GRBModel addBCEveryTeamPlaysOncePerWeek(GRBModel model) throws GRBException {
        for (int i = 0; i < this.slots.size(); i++) {
            for (Team t : this.teams) {
                GRBLinExpr expr = new GRBLinExpr();
                int index = i;
                List<MatchDataWrapper> list = this.variableByMatch.keySet().stream().filter(matchDataWrapper ->
                        matchDataWrapper.getRound() == index
                                && (matchDataWrapper.getMatch().getHomeTeam().equals(t)
                                || matchDataWrapper.getMatch().getAwayTeam().equals(t))
                ).collect(Collectors.toList());
                for (MatchDataWrapper mdw : list) {
                    expr.addTerm(1.0, this.variableByMatch.get(mdw));
                }
                model.addConstr(expr, GRB.EQUAL, 1.0, "C0_ONE_MATCH_PER_WEEK_" + t.getId() + '#' + i);
            }
        }
        return model;
    }

    public GRBModel addBCEveryTeamPlaysWithEachOppOnceAtHomeAndOnceAway(GRBModel model) throws GRBException {
        for (Match m : matches) {
            GRBLinExpr expr = new GRBLinExpr();
            List<MatchDataWrapper> list = variableByMatch.keySet().stream().filter(matchDataWrapper ->
                    matchDataWrapper.getMatch().equals(m)
            ).collect(Collectors.toList());
            for (MatchDataWrapper mdw : list) {
                expr.addTerm(1.0, variableByMatch.get(mdw));
            }
            model.addConstr(expr, GRB.EQUAL, 1.0, "C0_ONE_HOME_ONE_AWAY_" + m.getIndex());
        }
        return model;
    }
}
