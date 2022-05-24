package logic.constraints.separationConstraints;

import logic.constraints.SuperConstraint;
import logic.entities.ConstraintType;

import java.util.List;
import java.util.Objects;

public class SE1 extends SuperConstraint {
    private String mode1;
    private int min;
    private int penalty;
    private String teamGroups;
    private List<Integer> teams;
    private ConstraintType type;

    public SE1(List<Object> list) {
        this.mode1 = (String) list.get(0);
        this.min = (int) list.get(1);
        this.penalty = (int) list.get(2);
        this.teamGroups = (String) list.get(3);
        this.teams = (List<Integer>) list.get(4);
        this.type = (ConstraintType) list.get(5);
    }

    public String getMode1() {
        return mode1;
    }

    public void setMode1(String mode1) {
        this.mode1 = mode1;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public String getTeamGroups() {
        return teamGroups;
    }

    public void setTeamGroups(String teamGroups) {
        this.teamGroups = teamGroups;
    }

    public List<Integer> getTeams() {
        return teams;
    }

    public void setTeams(List<Integer> teams) {
        this.teams = teams;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SE1{");
        sb.append("mode1='").append(mode1).append('\'');
        sb.append(", min=").append(min);
        sb.append(", penalty=").append(penalty);
        sb.append(", teamGroups='").append(teamGroups).append('\'');
        sb.append(", teams=").append(teams);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public ConstraintType getType() {
        return this.type;
    }

    @Override
    public void setType(ConstraintType type) {
        this.type = type;
    }

    public int getPenalty() {
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SE1 se1 = (SE1) o;
        return min == se1.min &&
                penalty == se1.penalty &&
                mode1.equals(se1.mode1) &&
                teams.equals(se1.teams) &&
                type == se1.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode1, min, penalty, teams, type);
    }
}
