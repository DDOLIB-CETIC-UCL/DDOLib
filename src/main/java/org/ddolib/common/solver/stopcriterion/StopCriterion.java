package org.ddolib.common.solver.stopcriterion;

import org.ddolib.common.solver.stat.SearchStatistics;

import java.util.function.Predicate;

/**
 * Interface representing a criterion to stop the search process in a solver.
 * <p>
 * A StopCriterion is a {@link Predicate} that takes {@link SearchStatistics} as input
 * and returns {@code true} if the search should be terminated.
 * </p>
 */
@FunctionalInterface
public interface StopCriterion extends Predicate<SearchStatistics<?>> {

    /**
     * Creates a stop criterion that terminates the search when the total runtime
     * exceeds a specified limit.
     *
     * @param maxTimeMs the maximum allowed runtime in milliseconds
     * @return a StopCriterion for maximum runtime
     */
    static StopCriterion maxTime(long maxTimeMs) {
        return stats -> stats.runtime() >= maxTimeMs;
    }

    /**
     * Creates a stop criterion that terminates the search when the number of iterations
     * exceeds a specified limit.
     *
     * @param maxIter the maximum allowed number of iterations
     * @return a StopCriterion for maximum iterations
     */
    static StopCriterion maxIter(int maxIter) {
        return stats -> stats.nbIterations() >= maxIter;
    }

    /**
     * Creates a stop criterion that terminates the search if no improvement has been found
     * for a certain amount of time.
     *
     * @param maxTimeMs the maximum allowed time since the last improvement (in milliseconds)
     * @return a StopCriterion based on time since last improvement
     */
    static StopCriterion maxTimeSinceLastImprovement(long maxTimeMs) {
        return stats -> (stats.currentTime() - stats.lastTimeOfImprovement()) >= maxTimeMs;
    }

    /**
     * Creates a stop criterion that terminates the search if no improvement has been found
     * for a certain number of iterations.
     *
     * @param maxIter the maximum allowed number of iterations since the last improvement
     * @return a StopCriterion based on iterations since last improvement
     */
    static StopCriterion maxIterSinceLastImprovement(int maxIter) {
        return stats -> (stats.nbIterations() - stats.lastIterationOfImprovement()) >= maxIter;
    }

    /**
     * Creates a stop criterion that terminates the search when the relative improvement
     * between the current incumbent and the previous incumbent is less than or equal to a threshold.
     *
     * @param threshold the minimum relative improvement required to continue the search in percent
     * @return a StopCriterion based on the relative improvement
     */
    static StopCriterion minRelativeImprovement(double threshold) {
        return stats -> stats.relativeImprovement() <= threshold;
    }


}
