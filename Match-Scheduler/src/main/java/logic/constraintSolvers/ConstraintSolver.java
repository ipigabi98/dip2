package logic.constraintSolvers;

import logic.constraints.SuperConstraint;
import logic.entities.ConstraintType;
import gurobi.*;
import logic.scheduler.MatchScheduler;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ConstraintSolver {
    protected MatchScheduler scheduler;
    protected List<? extends SuperConstraint> constraints;
    protected List<? extends SuperConstraint> hardConstraints;
    protected List<? extends SuperConstraint> softConstraints;

    public abstract GRBModel addHardConstraints(GRBModel model) throws GRBException;
    public abstract GRBModel addSoftConstraints(GRBModel model) throws GRBException;

    protected static List<SuperConstraint> filterListByType(List<? extends SuperConstraint> list, ConstraintType type) {
        return list.stream().filter(element -> element.getType().equals(type)).collect(Collectors.toList());
    }

    protected GRBVar getMaxResult(GRBModel model, GRBLinExpr expr, String name) throws GRBException {
        String className = getConstraintTypeName();
        GRBVar maxHelper = model.addVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0, GRB.INTEGER, "MAX_HELPER_" + className + "#" + name);
        model.addConstr(maxHelper, GRB.EQUAL, expr, "MAX_HELPER_EQUATION_" + className + "#" + name);

        GRBVar[] maxHelpers = new GRBVar[1];
        maxHelpers[0] = maxHelper;

        GRBVar maxResult = model.addVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0, GRB.INTEGER, "MAX_RESULT_" + className + "#" + name);
        model.addGenConstrMax(maxResult, maxHelpers, 0.0, "MAX_RESULT_OBJ_" + className + "#" + name);

        return maxResult;
    }

    protected GRBVar getMinResult(GRBModel model, GRBLinExpr expr, String name) throws GRBException {
        String className = getConstraintTypeName();
        GRBVar minHelper = model.addVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0, GRB.INTEGER, "MIN_HELPER_" + className + "#" + name);
        model.addConstr(minHelper, GRB.EQUAL, expr, "MIN_HELPER_EQUATION_" + className + "#" + name);

        GRBVar[] minHelpers = new GRBVar[1];
        minHelpers[0] = minHelper;

        GRBVar minResult = model.addVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0, GRB.INTEGER, "MIN_RESULT_" + className + "#" + name);
        model.addGenConstrMax(minResult, minHelpers, 0.0, "MIN_RESULT_OBJ_" + className + "#" + name);

        return minResult;
    }

    protected String getConstraintTypeName() {
        String fullClassName = this.getClass().getName();
        String[] classNameList = fullClassName.split("\\.");
        String className = classNameList[classNameList.length - 1];
        int index = className.indexOf("Constraint");
        return className.substring(0, index);
    }
}
