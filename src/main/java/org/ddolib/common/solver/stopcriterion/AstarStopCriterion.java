package org.ddolib.common.solver.stopcriterion;

import org.ddolib.common.solver.stat.AstarStats;
import org.ddolib.common.solver.stat.SearchStatistics;

/**
 * Interface representing a stop criterion specifically for A* search variants.
 * <p>
 * This extends {@link StopCriterion} to provide access to A*-specific statistics
 * such as the valid children percentage.
 * </p>
 */
@FunctionalInterface
public interface AstarStopCriterion extends StopCriterion {

    /**
     * Creates a stop criterion that terminates the search when the smoothed percentage
     * of valid children falls below or equal to a specified threshold.
     *
     * @param minPercent the minimum required percentage of valid children to continue
     * @return an AstarStopCriterion for minimum valid children percentage
     */
    static AstarStopCriterion minValidChildrenPercent(double minPercent) {
        return stats -> stats.validChildrenPercent() <= minPercent;
    }

    /**
     * Evaluates this criterion against the given A* search statistics.
     *
     * @param stats the A* search statistics
     * @return {@code true} if the search should be terminated, {@code false} otherwise
     */
    boolean testAstarStats(AstarStats stats);

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the provided statistics are not an instance of {@link AstarStats}
     */
    @Override
    default boolean test(SearchStatistics<?> stats) {
        if (stats instanceof AstarStats aStarStats) {
            return testAstarStats(aStarStats);
        }
        throw new IllegalArgumentException("AStarStopCriterion requires AstarSearchStatistics.");
    }
}
