package logic.constraintSplitters;

import logic.constraints.SuperConstraint;
import logic.constraints.fairnessConstraints.FA2;
import logic.entities.ConstraintType;

import java.util.ArrayList;
import java.util.List;

public class FA2Splitter implements Splitter {
    private final List<FA2> originalConstraints;

    public FA2Splitter(List<FA2> originalConstraints) {
        this.originalConstraints = originalConstraints;
    }

    @Override
    public List<? extends SuperConstraint> splitConstraint() {
        List<FA2> result = new ArrayList<>();

        int id = 1;
        for (FA2 fa2 : this.originalConstraints) {
            int intp = fa2.getIntp();
            String mode = fa2.getMode();
            int penalty = fa2.getPenalty();
            ConstraintType type = fa2.getType();
            List<String> combinations = getAllCombinations(fa2.getTeams());

            for (int round : fa2.getSlots()) {

                for (String match : combinations) {
                    int team1Id = Integer.parseInt(match.split(",")[0]);
                    int team2Id = Integer.parseInt(match.split(",")[1]);

                    FA2 newConstraint = new FA2(String.valueOf(id));
                    newConstraint.setIntp(intp);
                    newConstraint.setMode(mode);
                    newConstraint.setPenalty(penalty);
                    newConstraint.setSlots(new ArrayList<Integer>(List.of(round)));
                    newConstraint.setType(type);
                    newConstraint.setTeams(new ArrayList<Integer>(List.of(team1Id, team2Id)));

                    result.add(newConstraint);
                    id++;
                }
            }
        }

        return result;
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
