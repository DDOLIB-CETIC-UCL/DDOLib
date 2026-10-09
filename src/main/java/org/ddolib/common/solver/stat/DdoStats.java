package org.ddolib.common.solver.stat;

/** Class tracking statistics specific to Decision Diagram Optimization (DDO) solver. */
public class DdoStats extends SearchStatistics {

    /** Total number of nodes created in all compiled MDDs. */
    private long totalNodes = 0;

    /** Maximum depth of a subproblem root popped from the frontier and explored. */
    private int maxExploredDepth = 0;

    /** Best (highest) global lower bound found so far. */
    private double bestLowerBound = Double.NEGATIVE_INFINITY;

    /** Iteration during which the last lower bound improvement was found. */
    private int lastIterationOfLowerBoundImprovement = 0;

    /**
     * Constructs a new SearchStatistics instance.
     *
     * @param startTime the start time of the search (in milliseconds)
     * @param initValue the initial value for the incumbent
     */
    public DdoStats(long startTime, double initValue) {
        super(startTime, initValue);
    }

    /** {@inheritDoc} */
    @Override
    protected DdoStats createSpecificInstance() {
        DdoStats clone = new DdoStats(this._startTime, this._incumbent);
        clone.totalNodes = this.totalNodes;
        clone.maxExploredDepth = this.maxExploredDepth;
        clone.bestLowerBound = this.bestLowerBound;
        clone.lastIterationOfLowerBoundImprovement = this.lastIterationOfLowerBoundImprovement;
        return clone;
    }

    /** {@inheritDoc} */
    @Override
    public DdoStats copy() {
        DdoStats clone = (DdoStats) super.copy();
        clone.totalNodes = this.totalNodes;
        clone.maxExploredDepth = this.maxExploredDepth;
        clone.bestLowerBound = this.bestLowerBound;
        clone.lastIterationOfLowerBoundImprovement = this.lastIterationOfLowerBoundImprovement;
        return clone;
    }

    /** {@inheritDoc} */
    @Override
    public DdoStats updateIncumbent(double incumbent, double gap) {
        return (DdoStats) super.updateIncumbent(incumbent, gap);
    }

    /** {@inheritDoc} */
    @Override
    public DdoStats updateStatus(SearchStatus status) {
        return (DdoStats) super.updateStatus(status);
    }

    /** {@inheritDoc} */
    @Override
    public DdoStats incrementNbIter() {
        return (DdoStats) super.incrementNbIter();
    }

    /** {@inheritDoc} */
    @Override
    public DdoStats updateFrontierMaxSize(int frontierSize) {
        return (DdoStats) super.updateFrontierMaxSize(frontierSize);
    }

    /** {@inheritDoc} */
    @Override
    public DdoStats updateGap(double gap) {
        return (DdoStats) super.updateGap(gap);
    }

    /** {@inheritDoc} */
    @Override
    public DdoStats updateTime(long time) {
        return (DdoStats) super.updateTime(time);
    }

    /**
     * Returns the total number of nodes created in all compiled MDDs.
     *
     * @return the total number of nodes
     */
    public long totalNodes() {
        return totalNodes;
    }

    /**
     * Returns the maximum depth reached during the search.
     *
     * @return the maximum depth
     */
    public int maxExploredDepth() {
        return maxExploredDepth;
    }

    /**
     * Returns the best (highest) global lower bound found so far.
     *
     * @return the best lower bound
     */
    public double bestLowerBound() {
        return bestLowerBound;
    }

    /**
     * Returns the iteration during which the last lower bound improvement was found.
     *
     * @return the last iteration of lower bound improvement
     */
    public int lastIterationOfLowerBoundImprovement() {
        return lastIterationOfLowerBoundImprovement;
    }

    /**
     * Updates the total number of nodes created.
     *
     * @param nodes the number of nodes to add
     * @return a new DdoStats instance with updated totalNodes
     */
    public DdoStats addNodes(int nodes) {
        DdoStats toReturn = this.copy();
        toReturn.totalNodes += nodes;
        return toReturn;
    }

    /**
     * Updates the maximum depth reached.
     *
     * @param depth the current depth
     * @return a new DdoStats instance with updated maxExploredDepth
     */
    public DdoStats updateMaxDepth(int depth) {
        DdoStats toReturn = this.copy();
        toReturn.maxExploredDepth = Math.max(this.maxExploredDepth, depth);
        return toReturn;
    }

    /**
     * Updates the best lower bound and tracks its improvement iteration.
     *
     * @param lb the new lower bound
     * @return a new DdoStats instance with updated bestLowerBound
     */
    public DdoStats updateLowerBound(double lb) {
        DdoStats toReturn = this.copy();
        if (lb > this.bestLowerBound) {
            toReturn.bestLowerBound = lb;
            toReturn.lastIterationOfLowerBoundImprovement = this._nbIterations;
        }
        return toReturn;
    }
}
