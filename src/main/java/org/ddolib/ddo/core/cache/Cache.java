package org.ddolib.ddo.core.cache;

import org.ddolib.ddo.core.SubProblem;
import org.ddolib.modeling.Problem;

import java.util.Optional;

/**
 * Defines the abstraction of a <b>cache mechanism</b> used to prune and reduce the
 * search space during the compilation or exploration of a Decision Diagram (DD).
 * <p>
 * The cache stores <em>thresholds</em> associated with subproblems encountered
 * during the search. These thresholds represent bounds (on value or feasibility)
 * that can be reused to avoid re-exploring equivalent or dominated states in
 * subsequent iterations or at different depths of the DD.
 * </p>
 *
 * <p>By maintaining and comparing thresholds, the cache helps:
 * </p>
 * <ul>
 *   <li>Prevent redundant computation on subproblems already explored with
 *       better or equivalent objective values.</li>
 *   <li>Accelerate convergence of relaxed DDs by reusing partial results.</li>
 *   <li>Reduce memory footprint by discarding obsolete layers.</li>
 * </ul>
 *
 * <p>This interface defines the operations required for cache initialization,
 * lookup, update, and cleanup at various layers of the decision diagram.</p>
 *
 * @param <T> the type representing the problem states stored in the cache
 *
 * @see Threshold
 * @see SimpleCache
 * @see SubProblem
 * @see Problem
 */

public interface Cache<T> {
    /**
     * Determines whether the given subproblem must still be explored,
     * based on the information currently stored in the cache.
     * <p>
     * This method checks if there exists a {@link Threshold} in the cache
     * associated with the same state and depth. If such a threshold exists
     * and its stored value is <em>better or equal</em> than the subproblem’s
     * current value, the subproblem may be safely skipped.
     * </p>
     *
     * @param subproblem the subproblem being considered for expansion in the DD
     * @param depth      the current depth (layer index) in the DD
     * @return {@code true} if the subproblem should still be explored;
     *         {@code false} if it can be pruned using cached thresholds
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
     * Initializes the cache for use with the specified problem instance.
     * <p>
     * This method is typically called once before the DD compilation begins.
     * It prepares the internal data structures to store thresholds for the
     * given problem states and layers.
     * </p>
     *
     * @param problem the problem instance for which the cache will be used
     */
    void initialize(final Problem<T> problem);

    /**
     * Retrieves the cache layer associated with the specified depth.
     * <p>
     * Each layer maintains the thresholds of all states encountered at a
     * specific depth of the decision diagram.
     * </p>
     *
     * @param depth the depth (layer index) of the DD
     * @return the {@link SimpleCache.Layer} object containing thresholds
     *         for the specified layer
     */
    SimpleCache.Layer<T> getLayer(int depth);

    /**
     * Retrieves the threshold currently associated with a given state and depth.
     *
     * @param state the state whose threshold is being requested
     * @param depth the depth (layer) where the state resides
     * @return an {@link Optional} containing the corresponding {@link Threshold}
     *         if present, or an empty {@link Optional} otherwise
     */
    Optional<Threshold> getThreshold(final T state, int depth);

    /**
     * Updates the threshold associated with a given state at a given depth.
     * <p>
     * The threshold is updated only if the new value is an <em>improvement</em>
     * (e.g., higher for maximization or lower for minimization problems)
     * over the one currently stored in the cache.
     * </p>
     *
     * @param state      the state whose threshold is to be updated
     * @param depth      the depth (layer index) in the DD
     * @param threshold  the new threshold to associate with the state
     */
    void updateThreshold(final T state, final int depth, Threshold threshold);

    /**
     * Removes all thresholds associated with states at the specified depth.
     * <p>
     * This operation is useful to reclaim memory once a DD layer has been
     * fully processed or when restarting a computation from a specific level.
     * </p>
     *
     * @param depth the depth (layer index) to clear
     */
    void clearLayer(final int depth);

    /**
     * Clears cache data up to a specified depth.
     * <p>
     * This operation removes cached thresholds from the beginning of the DD
     * up to (and possibly including) the specified layer index, depending on
     * the implementation.
     * </p>
     *
     * @param n the depth up to which to clear the cache
     */
    void clear(int n);

}
