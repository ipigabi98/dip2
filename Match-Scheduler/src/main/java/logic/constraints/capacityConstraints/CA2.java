package logic.constraints.capacityConstraints;

import logic.constraints.SuperConstraint;
import logic.entities.ConstraintType;

import java.util.List;

public class CA2 extends SuperConstraint {
    private int max;
    private int min;
    private String mode1;
    private String mode2;
    private int penalty;
    private String slotGroups;
    private List<Integer> slots;
    private String teamGroups1;
    private String teamGroups2;
    private String teams1;
    private List<Integer> teams2;
    private ConstraintType type;

    public CA2(List<Object> list) {
        this.max = (int) list.get(0);
        this.min = (int) list.get(1);
        this.mode1 = (String) list.get(2);
        this.mode2 = (String) list.get(3);
        this.penalty = (int) list.get(4);
        this.slotGroups = (String) list.get(5);
        this.slots = (List<Integer>) list.get(6);
        this.teamGroups1 = (String) list.get(7);
        this.teamGroups2 = (String) list.get(8);
        this.teams1 = (String) list.get(9);
        this.teams2 = (List<Integer>) list.get(10);;
        this.type = (ConstraintType) list.get(11);
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

    public String getSlotGroups() {
        return slotGroups;
    }

    public void setSlotGroups(String slotGroups) {
        this.slotGroups = slotGroups;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public void setSlots(List<Integer> slots) {
        this.slots = slots;
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

    public String getTeams1() {
        return teams1;
    }

    public void setTeams1(String teams1) {
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
        final StringBuilder sb = new StringBuilder("CA2{");
        sb.append("max=").append(max);
        sb.append(", min=").append(min);
        sb.append(", mode1='").append(mode1).append('\'');
        sb.append(", mode2='").append(mode2).append('\'');
        sb.append(", penalty=").append(penalty);
        sb.append(", slotGroups='").append(slotGroups).append('\'');
        sb.append(", slots=").append(slots);
        sb.append(", teamGroups1='").append(teamGroups1).append('\'');
        sb.append(", teamGroups2='").append(teamGroups2).append('\'');
        sb.append(", teams1='").append(teams1).append('\'');
        sb.append(", teams2='").append(teams2).append('\'');
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
}
