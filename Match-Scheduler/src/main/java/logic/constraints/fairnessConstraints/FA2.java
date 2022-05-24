package logic.constraints.fairnessConstraints;

import logic.constraints.SuperConstraint;
import logic.entities.ConstraintType;

import java.util.List;

public class FA2 extends SuperConstraint {
    private int intp;
    private String mode;
    private int penalty;
    private String slotGroups;
    private List<Integer> slots;
    private String teamGroups;
    private List<Integer> teams;
    private ConstraintType type;
    private String id;

    public FA2(String id) {
        this.id = id;
    }

    public FA2(List<Object> list) {
        this.intp = (int) list.get(0);
        this.mode = (String) list.get(1);
        this.penalty = (int) list.get(2);
        this.slotGroups = (String) list.get(3);
        this.slots = (List<Integer>) list.get(4);
        this.teamGroups = (String) list.get(5);
        this.teams = (List<Integer>) list.get(6);
        this.type = (ConstraintType) list.get(7);
    }

    public String getId() {
        return id;
    }

    public int getIntp() {
        return intp;
    }

    public void setIntp(int intp) {
        this.intp = intp;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
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

    public List<Integer> getTeams() {
        return teams;
    }

    public void setTeams(List<Integer> teams) {
        this.teams = teams;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FA2{");
        sb.append("intp=").append(intp);
        sb.append(", mode='").append(mode).append('\'');
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
