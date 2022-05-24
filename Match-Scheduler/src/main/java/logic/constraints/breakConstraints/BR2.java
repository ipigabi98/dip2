package logic.constraints.breakConstraints;

import logic.constraints.SuperConstraint;
import logic.entities.ConstraintType;

import java.util.List;
import java.util.Objects;

public class BR2 extends SuperConstraint {
    private int intp;
    private String homeMode;
    private String mode2;
    private int penalty;
    private String slotGroups;
    private List<Integer> slots;
    private String teamGroups;
    private List<Integer> teams;
    private ConstraintType type;

    public BR2(List<Object> list) {
        this.intp = (int) list.get(0);
        this.homeMode = (String) list.get(1);
        this.mode2 = (String) list.get(2);
        this.penalty = (int) list.get(3);
        this.slotGroups = (String) list.get(4);
        this.slots = (List<Integer>) list.get(5);
        this.teamGroups = (String) list.get(6);
        this.teams = (List<Integer>) list.get(7);
        this.type = (ConstraintType) list.get(8);
    }

    public int getIntp() {
        return intp;
    }

    public void setIntp(int intp) {
        this.intp = intp;
    }

    public String getHomeMode() {
        return homeMode;
    }

    public void setHomeMode(String homeMode) {
        this.homeMode = homeMode;
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

    public List<Integer> getTeams() {
        return teams;
    }

    public void setTeams(List<Integer> teams) {
        this.teams = teams;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BR2{");
        sb.append("intp=").append(intp);
        sb.append(", homeMode='").append(homeMode).append('\'');
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BR2 br2 = (BR2) o;
        return intp == br2.intp &&
                penalty == br2.penalty &&
                homeMode.equals(br2.homeMode) &&
                mode2.equals(br2.mode2) &&
                slots.equals(br2.slots) &&
                teams.equals(br2.teams) &&
                type == br2.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(intp, homeMode, mode2, penalty, slots, teams, type);
    }
}
