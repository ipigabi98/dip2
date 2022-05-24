package logic.constraintSplitters;

import logic.constraints.SuperConstraint;

import java.util.List;

public interface Splitter {
    List<? extends SuperConstraint> splitConstraint();
}
