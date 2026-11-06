package org.ddolib.common.solver.stopcriterion;

import java.util.function.Predicate;
import org.ddolib.common.solver.stat.SearchStatistics;

/**
 * Interface representing a criterion to stop the search process in a solver.
 *
 * <p>A StopCriterion is a {@link Predicate} that takes {@link SearchStatistics} as input and
 * returns {@code true} if the search should be terminated.
 */
@FunctionalInterface
public interface StopCriterion extends Predicate<SearchStatistics> {

    /**
     * Creates a stop criterion that terminates the search when the total runtime exceeds a
     * specified limit.
     *
     * @param maxTimeMs the maximum allowed runtime in milliseconds
     * @return a StopCriterion for maximum runtime
     */
    static StopCriterion maxTime(long maxTimeMs) {
        return stats -> stats.runtime() >= maxTimeMs;
    }

    /**
     * Creates a stop criterion that terminates the search when the number of iterations exceeds a
     * specified limit.
     *
     * @param maxIter the maximum allowed number of iterations
     * @return a StopCriterion for maximum iterations
     */
    static StopCriterion maxIter(int maxIter) {
        return stats -> stats.nbIterations() >= maxIter;
    }

    /**
     * Creates a stop criterion that terminates the search if no improvement has been found for a
     * certain amount of time.
     *
     * @param maxTimeMs the maximum allowed time since the last improvement (in milliseconds)
     * @return a StopCriterion based on time since last improvement
     */
    static StopCriterion maxTimeSinceLastImprovement(long maxTimeMs) {
        return stats -> (stats.currentTime() - stats.lastTimeOfImprovement()) >= maxTimeMs;
    }

    /**
     * Creates a stop criterion that terminates the search if no improvement has been found for a
     * certain number of iterations.
     *
     * @param maxIter the maximum allowed number of iterations since the last improvement
     * @return a StopCriterion based on iterations since last improvement
     */
    static StopCriterion maxIterSinceLastImprovement(int maxIter) {
        return stats -> (stats.nbIterations() - stats.lastIterationOfImprovement()) >= maxIter;
    }

    /**
     * Creates a stop criterion that terminates the search when the relative improvement between the
     * current incumbent and the previous incumbent is less than or equal to a threshold.
     *
     * @param threshold the minimum relative improvement required to continue the search in percent
     * @return a StopCriterion based on the relative improvement
     */
    static StopCriterion minRelativeImprovement(double threshold) {
        return stats -> stats.relativeImprovement() <= threshold;
    }

    /**
     * Creates a stop criterion that terminates the search when the optimality gap is less than or
     * equal to a specified threshold.
     *
     * @param gapPercent the maximum allowed optimality gap in percent
     * @return a StopCriterion for minimum optimality gap
     */
    static StopCriterion minGap(double gapPercent) {
        return stats -> stats.gap() <= gapPercent;
    }

    /**
     * Creates a memory limit that terminates the search when the frontier (or the open list)
     * reaches a specified size. The solver then returns the best solution found so far with the
     * status {@link org.ddolib.common.solver.stat.SearchStatus#MEMOUT}.
     *
     * <p>The limit is signaled by throwing a {@link MemoryLimitException}, so that the solver can
     * tell it from the other criteria, even when it is combined with them (for instance {@code
     * maxTime(t).or(maxFrontierSize(n))}). The solvers catch this exception: it is only seen when
     * the criterion is tested outside of a solver.
     *
     * @param maxNodes the maximum allowed size of the frontier
     * @return a StopCriterion for maximum frontier size
     */
    static StopCriterion maxFrontierSize(int maxNodes) {
        return stats -> {
            if (stats.frontierMaxSize() >= maxNodes) {
                throw new MemoryLimitException(
                        "The frontier reached the maximum size of " + maxNodes + " nodes");
            }
            return false;
        };
    }

    /**
     * Creates a stop criterion that terminates the search when the number of iterations without any
     * gap improvement exceeds a specified limit.
     *
     * @param maxIter the maximum allowed number of iterations since the last gap improvement
     * @return a StopCriterion based on iterations since last gap improvement
     */
    static StopCriterion maxIterWithoutGapImprovement(int maxIter) {
        return stats -> (stats.nbIterations() - stats.lastIterationOfGapImprovement()) >= maxIter;
    }
}
