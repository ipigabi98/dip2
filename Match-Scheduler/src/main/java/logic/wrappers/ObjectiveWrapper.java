package logic.wrappers;

import gurobi.GRBLinExpr;

public class ObjectiveWrapper {
    private final int penalty;
    private final GRBLinExpr expr;
    private final ObjectiveMode mode;
    private final int value;

    public ObjectiveWrapper(int penalty, GRBLinExpr expr, ObjectiveMode mode, int value) {
        this.penalty = penalty;
        this.expr = expr;
        this.mode = mode;
        this.value = value;
    }

    public int getPenalty() {
        return penalty;
    }

    public GRBLinExpr getExpr() {
        return expr;
    }

    public ObjectiveMode getMode() {
        return mode;
    }

    public int getValue() {
        return value;
    }
}
