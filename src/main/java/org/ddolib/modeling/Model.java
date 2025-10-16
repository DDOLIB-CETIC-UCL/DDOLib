package org.ddolib.modeling;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.common.solver.SearchStatistics;

public interface Model<T> {

    Problem<T> problem();

    default FastLowerBound<T> lowerBound() {
        return new DefaultFastLowerBound<>();
    }

    default DominanceChecker<T> dominance() {
        return new DefaultDominanceChecker<>();
    }

    default void onSolution(SearchStatistics statistics) {
    }

    default VariableHeuristic<T> variableHeuristic() {
        return new DefaultVariableHeuristic<>();
    }

    default VerbosityLevel verbosityLevel() {
        return VerbosityLevel.NORMAL;
    }

    default DebugLevel debugMode() {
        return DebugLevel.OFF;
    }
}
