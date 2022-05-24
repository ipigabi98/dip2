package logic.constraints.breakConstraints;

import logic.constraints.SuperConstraint;
import logic.entities.ConstraintType;

import java.util.List;

public class BR1 extends SuperConstraint {
    private int intp;
    private String mode1;
    private String mode2;
    private int penalty;
    private String slotGroups;
    private List<Integer> slots;
    private String teamGroups;
    private int teams;
    private ConstraintType type;

    public BR1(List<Object> list) {
        this.intp = (int) list.get(0);
        this.mode1 = (String) list.get(1);
        this.mode2 = (String) list.get(2);
        this.penalty = (int) list.get(3);
        this.slotGroups = (String) list.get(4);
        this.slots = (List<Integer>) list.get(5);
        this.teamGroups = (String) list.get(6);
        this.teams = (int) list.get(7);
        this.type = (ConstraintType) list.get(8);
    }

    public int getIntp() {
        return intp;
    }

    public void setIntp(int intp) {
        this.intp = intp;
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

    public String getTeamGroups() {
        return teamGroups;
    }

    public void setTeamGroups(String teamGroups) {
        this.teamGroups = teamGroups;
    }

    public int getTeams() {
        return teams;
    }

    public void setTeams(int teams) {
        this.teams = teams;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BR1{");
        sb.append("intp=").append(intp);
        sb.append(", mode1='").append(mode1).append('\'');
        sb.append(", mode2='").append(mode2).append('\'');
        sb.append(", penalty=").append(penalty);
        sb.append(", slotGroups='").append(slotGroups).append('\'');
        sb.append(", slots='").append(slots).append('\'');
        sb.append(", teamGroups='").append(teamGroups).append('\'');
        sb.append(", teams='").append(teams).append('\'');
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
