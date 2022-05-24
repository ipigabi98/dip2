package logic.constraints.capacityConstraints;

import logic.constraints.SuperConstraint;
import logic.entities.ConstraintType;

import java.util.List;
import java.util.Objects;

public class CA1 extends SuperConstraint {
    private int max;
    private int min;
    private String mode;
    private int penalty;
    private String slotGroups;
    private List<Integer> slots;
    private String teamGroups;
    private String teams;
    private ConstraintType type;

    public CA1(List<Object> list) {
        this.max = (int) list.get(0);
        this.min = (int) list.get(1);
        this.mode = (String) list.get(2);
        this.penalty = (int) list.get(3);
        this.slotGroups = (String) list.get(4);
        this.slots = (List<Integer>) list.get(5);
        this.teamGroups = (String) list.get(6);
        this.teams = (String) list.get(7);
        this.type = (ConstraintType) list.get(8);
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

    public String getTeams() {
        return teams;
    }

    public void setTeams(String teams) {
        this.teams = teams;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CA1{");
        sb.append("max=").append(max);
        sb.append(", min=").append(min);
        sb.append(", mode='").append(mode).append('\'');
        sb.append(", penalty=").append(penalty);
        sb.append(", slotGroups='").append(slotGroups).append('\'');
        sb.append(", slots=").append(slots);
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
        CA1 ca1 = (CA1) o;
        return max == ca1.max &&
                min == ca1.min &&
                penalty == ca1.penalty &&
                mode.equals(ca1.mode) &&
                slots.equals(ca1.slots) &&
                teams.equals(ca1.teams) &&
                type == ca1.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(max, min, mode, penalty, slots, teams, type);
    }
}
