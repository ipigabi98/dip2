package logic.constraints;

import logic.entities.ConstraintType;

public abstract class SuperConstraint {
    protected int penalty;

    public abstract ConstraintType getType();

    public abstract void setType(ConstraintType type);
}
