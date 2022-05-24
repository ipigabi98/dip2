package logic.constraints.capacityConstraints;

import logic.constraints.SuperConstraint;
import logic.entities.ConstraintType;

import java.util.List;
import java.util.Objects;

public class CA3 extends SuperConstraint {
    private int intp;
    private int max;
    private int min;
    private String mode1;
    private String mode2;
    private int penalty;
    private String teamGroups1;
    private String teamGroups2;
    private List<Integer> teams1;
    private List<Integer> teams2;
    private ConstraintType type;

    public CA3(List<Object> list) {
        this.intp = (int) list.get(0);
        this.max = (int) list.get(1);
        this.min = (int) list.get(2);
        this.mode1 = (String) list.get(3);
        this.mode2 = (String) list.get(4);
        this.penalty = (int) list.get(5);
        this.teamGroups1 = (String) list.get(6);
        this.teamGroups2 = (String) list.get(7);
        this.teams1 = (List<Integer>) list.get(8);
        this.teams2 = (List<Integer>) list.get(9);;
        this.type = (ConstraintType) list.get(10);
    }

    public int getIntp() {
        return intp;
    }

    public void setIntp(int intp) {
        this.intp = intp;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public String getMode1() {
        return mode1;
    }

    public void setMode1(String mode1) {
        this.mode1 = mode1;
    }

    public String getMode2() {
        return mode2;
    }

    public void setMode2(String mode2) {
        this.mode2 = mode2;
    }

    public String getTeamGroups1() {
        return teamGroups1;
    }

    public void setTeamGroups1(String teamGroups1) {
        this.teamGroups1 = teamGroups1;
    }

    public String getTeamGroups2() {
        return teamGroups2;
    }

    public void setTeamGroups2(String teamGroups2) {
        this.teamGroups2 = teamGroups2;
    }

    public List<Integer> getTeams1() {
        return teams1;
    }

    public void setTeams1(List<Integer> teams1) {
        this.teams1 = teams1;
    }

    public List<Integer> getTeams2() {
        return teams2;
    }

    public void setTeams2(List<Integer> teams2) {
        this.teams2 = teams2;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CA3{");
        sb.append("intp=").append(intp);
        sb.append(", max=").append(max);
        sb.append(", min=").append(min);
        sb.append(", mode1='").append(mode1).append('\'');
        sb.append(", mode2='").append(mode2).append('\'');
        sb.append(", penalty=").append(penalty);
        sb.append(", teamGroups1='").append(teamGroups1).append('\'');
        sb.append(", teamGroups2='").append(teamGroups2).append('\'');
        sb.append(", teams1=").append(teams1);
        sb.append(", teams2=").append(teams2);
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
        CA3 ca3 = (CA3) o;
        return intp == ca3.intp &&
                max == ca3.max &&
                min == ca3.min &&
                penalty == ca3.penalty &&
                mode1.equals(ca3.mode1) &&
                mode2.equals(ca3.mode2) &&
                teams1.equals(ca3.teams1) &&
                teams2.equals(ca3.teams2) &&
                type == ca3.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(intp, max, min, mode1, mode2, penalty, teams1, teams2, type);
    }
}
