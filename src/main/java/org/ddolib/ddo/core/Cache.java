package org.ddolib.ddo.core;

import org.ddolib.ddo.implem.cache.SimpleCache;
import org.ddolib.ddo.implem.cache.Threshold;
import org.ddolib.ddo.modeling.Problem;

import java.util.Optional;

/**
 * Abstract of the cache use to reduce the search tree
 *
 * @param <T> the type of the state
 */

public interface Cache<T> {
    /**
     * Returns true if the subproblem still must be explored,
     * given the thresholds contained in the cache.
     */
    default boolean mustExplore(SubProblem<T> subproblem, int depth) {
        Optional<Threshold> thresholdOpt = getThreshold(subproblem.getState(), depth);
        if (thresholdOpt.isPresent()) {
            Threshold threshold = thresholdOpt.get();
            return subproblem.getValue() < threshold.getValue()
                    || (subproblem.getValue() == threshold.getValue() && threshold.isExplored());
        } else {
            return true;
        }
    }

    /**
     * Prepare the cache to be used with the given problem.
     */
    void initialize(final Problem<T> problem);

    /**
     * Return the thresholds of the states of layer depth
     */
    SimpleCache.Layer<T> getLayer(int depth);

    /**
     * Returns the threshold currently associated with the given state, if any.
     */
    Optional<Threshold> getThreshold(final T state, int depth);

    /**
     * Updates the threshold associated with the given state, only if it is increased.
     */
    void updateThreshold(final T state, final int depth, Threshold threshold);

    /**
     * Removes all thresholds associated with states at the given depth.
     */
    void clearLayer(final int depth);

    /**
     * Clears the data structure.
     */
    void clear(int n);

}
