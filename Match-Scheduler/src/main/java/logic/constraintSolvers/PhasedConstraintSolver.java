package logic.constraintSolvers;

import logic.entities.Match;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import logic.scheduler.MatchScheduler;
import logic.wrappers.MatchDataWrapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PhasedConstraintSolver {
    private MatchScheduler scheduler;

    public PhasedConstraintSolver(MatchScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public GRBModel addPhasedConstraints(GRBModel model) throws GRBException {
        List<Match> matches = this.scheduler.getMatches();
        Set<Match> calculatedMatches = new HashSet<>();
        int phaseSize = this.scheduler.getSlots().size()/2;

        for (Match match : matches) {
            int rematchIndex = getRematchIndex(matches, match);
            Match rematch = matches.get(rematchIndex);

            if (!calculatedMatches.contains(match) && !calculatedMatches.contains(rematch)) {
                List<MatchDataWrapper> sortedListP1 = sortList(match, rematch, phaseSize, true);
                List<MatchDataWrapper> sortedListP2 = sortList(match, rematch, phaseSize, false);

                GRBLinExpr expr1 = new GRBLinExpr();
                for (MatchDataWrapper mdw : sortedListP1) {
                    expr1.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                }
                model.addConstr(expr1, GRB.EQUAL, 1.0,  "PHASED_CONSTRAINT_PHASE_1_" + match.getHomeTeam().getId() + "-" + match.getAwayTeam().getId());

                GRBLinExpr expr2 = new GRBLinExpr();
                for (MatchDataWrapper mdw : sortedListP2) {
                    expr2.addTerm(1.0, this.scheduler.getVariableByMatch().get(mdw));
                }
                model.addConstr(expr2, GRB.EQUAL, 1.0,  "PHASED_CONSTRAINT_PHASE_2_" + match.getHomeTeam().getId() + "-" + match.getAwayTeam().getId());
            }

            calculatedMatches.add(match);
            calculatedMatches.add(rematch);
        }

        System.out.println(this.scheduler.getMatches().size());
        return model;
    }

    private int getRematchIndex(List<Match> matches, Match match) {
        int index = -1;
        for (int i = 0; i < matches.size(); i++) {
            Match m = matches.get(i);
            if (m.getHomeTeam() == match.getAwayTeam() && m.getAwayTeam() == match.getHomeTeam()) {
                index = i;
                break;
            }
        }
        return index;
    }

    private List<MatchDataWrapper> sortList(Match match, Match rematch, int phaseSize, boolean isFirstPhase) {
        if (isFirstPhase) {
            return this.scheduler.getVariableByMatch().keySet().stream()
                    .filter(mdw -> (mdw.getMatch() == match
                            || mdw.getMatch() == rematch)
                            && mdw.getRound() < phaseSize
                    )
                    .collect(Collectors.toList());
        } else {
            return this.scheduler.getVariableByMatch().keySet().stream()
                    .filter(mdw -> (mdw.getMatch() == match
                            || mdw.getMatch() == rematch)
                            && mdw.getRound() >= phaseSize
                    )
                    .collect(Collectors.toList());
        }

    }
}
