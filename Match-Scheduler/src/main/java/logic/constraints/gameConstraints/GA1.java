package logic.constraints.gameConstraints;

import logic.constraints.SuperConstraint;
import logic.entities.ConstraintType;

import java.util.List;
import java.util.Objects;

public class GA1 extends SuperConstraint {
    private int max;
    private List<List<Integer>> meetings;
    private int min;
    private int penalty;
    private String slotGroups;
    private List<Integer> slots;
    private ConstraintType type;

    public GA1(List<Object> list) {
        this.max = (int) list.get(0);
        this.meetings = (List<List<Integer>>) list.get(1);
        this.min = (int) list.get(2);
        this.penalty = (int) list.get(3);
        this.slotGroups = (String) list.get(4);
        this.slots = (List<Integer>) list.get(5);
        this.type = (ConstraintType) list.get(6);
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public List<List<Integer>> getMeetings() {
        return meetings;
    }

    public void setMeetings(List<List<Integer>> meetings) {
        this.meetings = meetings;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GA1{");
        sb.append("max=").append(max);
        sb.append(", meetings='").append(meetings).append('\'');
        sb.append(", min=").append(min);
        sb.append(", penalty=").append(penalty);
        sb.append(", slotGroups='").append(slotGroups).append('\'');
        sb.append(", slots='").append(slots).append('\'');
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
        GA1 ga1 = (GA1) o;
        return max == ga1.max &&
                min == ga1.min &&
                penalty == ga1.penalty &&
                meetings.equals(ga1.meetings) &&
                slots.equals(ga1.slots) &&
                type == ga1.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(max, meetings, min, penalty, slots, type);
    }
}
