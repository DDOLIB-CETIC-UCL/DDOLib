package org.ddolib.nolayer.modeling;

import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.verbosity.VerbosityLevel;

/**
 * Defines the core model interface for describing an optimization problem
 * in the NoLayer API.
 *
 * @param <T> the type representing the state space of the problem
 */
public interface Model<T> {

    /**
     * Returns the optimization problem instance associated with this model.
     *
     * @return the {@link Problem} defining the structure, transitions,
     * and objective function
     */
    Problem<T> problem();

    /**
     * Returns a heuristic that estimates a lower bound on the objective value
     * for a given state.
     *
     * @return the {@link FastLowerBound} heuristic
     */
    default FastLowerBound<T> lowerBound() {
        return new DefaultFastLowerBound<>();
    }

    /**
     * Returns a precomputed upper bound on the optimal value.
     *
     * @return a precomputed upper bound on the optimal value
     */
    default double upperBound() {
        return Double.POSITIVE_INFINITY;
    }

    /**
     * Returns the dominance checker used to prune dominated states.
     *
     * @return the {@link NoLayerDominanceChecker} used for dominance testing
     */
    default NoLayerDominanceChecker<T> dominance() {
        return new DefaultNoLayerDominanceChecker<>();
    }

    /**
     * Returns the verbosity level of the solver when this model is executed.
     *
     * @return the desired {@link VerbosityLevel}
     */
    default VerbosityLevel verbosityLevel() {
        return VerbosityLevel.SILENT;
    }

    /**
     * Returns the debugging level.
     *
     * @return the {@link DebugLevel} controlling debug behavior
     */
    default DebugLevel debugMode() {
        return DebugLevel.OFF;
    }
}
