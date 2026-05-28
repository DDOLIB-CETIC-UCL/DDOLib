package org.ddolib.common.solver.stopcriterion;

import org.ddolib.common.solver.stat.DdoStats;
import org.ddolib.common.solver.stat.SearchStatistics;

/**
 * Interface representing a stop criterion specifically for Decision Diagram Optimization (DDO) solvers.
 * <p>
 * This extends {@link StopCriterion} to provide access to DDO-specific statistics
 * such as the total number of MDD nodes or the explored depth.
 * </p>
 */
@FunctionalInterface
public interface DdoStopCriterion extends StopCriterion {

    /**
     * Creates a stop criterion that terminates the search when the total number
     * of MDD nodes created exceeds a specified limit.
     *
     * @param maxNodes the maximum allowed number of MDD nodes
     * @return a DdoStopCriterion for maximum total nodes
     */
    static DdoStopCriterion maxTotalMddNodes(long maxNodes) {
        return stats -> stats.totalNodes() >= maxNodes;
    }

    /**
     * Creates a stop criterion that terminates the search when the number of iterations
     * without any lower bound improvement exceeds a specified limit.
     *
     * @param maxIter the maximum allowed number of iterations since the last lower bound improvement
     * @return a DdoStopCriterion based on iterations since last lower bound improvement
     */
    static DdoStopCriterion maxIterWithoutLowerBoundImprovement(int maxIter) {
        return stats -> (stats.nbIterations() - stats.lastIterationOfLowerBoundImprovement()) >= maxIter;
    }

    /**
     * Creates a stop criterion that terminates the search when the depth of the
     * subproblem root being explored (the node popped from the frontier)
     * reaches or exceeds a specified limit.
     *
     * @param maxDepth the maximum allowed depth for a subproblem root
     * @return a DdoStopCriterion for maximum explored depth
     */
    static DdoStopCriterion maxExploredDepth(int maxDepth) {
        return stats -> stats.maxExploredDepth() >= maxDepth;
    }

    /**
     * Evaluates this criterion against the given DDO search statistics.
     *
     * @param stats the DDO search statistics
     * @return {@code true} if the search should be terminated, {@code false} otherwise
     */
    boolean testDdoStats(DdoStats stats);

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the provided statistics are not an instance of {@link DdoStats}
     */
    @Override
    default boolean test(SearchStatistics<?> stats) {
        if (stats instanceof DdoStats ddoStats) {
            return testDdoStats(ddoStats);
        }
        throw new IllegalArgumentException("DdoStopCriterion requires DdoStats.");
    }
}
