package org.ddolib.common.solver.stat;

/**
 * Class tracking statistics specific to Decision Diagram Optimization (DDO) solver.
 */
public class DdoStats extends SearchStatistics<DdoStats> {

    /**
     * Total number of nodes created in all compiled MDDs.
     */
    private long _totalNodes = 0;

    /**
     * Maximum depth of a subproblem root popped from the frontier and explored.
     */
    private int _maxExploredDepth = 0;

    /**
     * Best (highest) global lower bound found so far.
     */
    private double _bestLowerBound = Double.NEGATIVE_INFINITY;

    /**
     * Iteration during which the last lower bound improvement was found.
     */
    private int _lastIterationOfLowerBoundImprovement = 0;

    /**
     * Constructs a new SearchStatistics instance.
     *
     * @param startTime the start time of the search (in milliseconds)
     * @param initValue the initial value for the incumbent
     */
    public DdoStats(long startTime, double initValue) {
        super(startTime, initValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DdoStats createSpecificInstance() {
        DdoStats clone = new DdoStats(this._startTime, this._incumbent);
        clone._totalNodes = this._totalNodes;
        clone._maxExploredDepth = this._maxExploredDepth;
        clone._bestLowerBound = this._bestLowerBound;
        clone._lastIterationOfLowerBoundImprovement = this._lastIterationOfLowerBoundImprovement;
        return clone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DdoStats copy() {
        DdoStats clone = super.copy();
        clone._totalNodes = this._totalNodes;
        clone._maxExploredDepth = this._maxExploredDepth;
        clone._bestLowerBound = this._bestLowerBound;
        clone._lastIterationOfLowerBoundImprovement = this._lastIterationOfLowerBoundImprovement;
        return clone;
    }

    /**
     * Returns the total number of nodes created in all compiled MDDs.
     * @return the total number of nodes
     */
    public long totalNodes() {
        return _totalNodes;
    }

    /**
     * Returns the maximum depth reached during the search.
     * @return the maximum depth
     */
    public int maxExploredDepth() {
        return _maxExploredDepth;
    }

    /**
     * Returns the best (highest) global lower bound found so far.
     * @return the best lower bound
     */
    public double bestLowerBound() {
        return _bestLowerBound;
    }

    /**
     * Returns the iteration during which the last lower bound improvement was found.
     * @return the last iteration of lower bound improvement
     */
    public int lastIterationOfLowerBoundImprovement() {
        return _lastIterationOfLowerBoundImprovement;
    }

    /**
     * Updates the total number of nodes created.
     * @param nodes the number of nodes to add
     * @return a new DdoStats instance with updated totalNodes
     */
    public DdoStats addNodes(int nodes) {
        DdoStats toReturn = this.copy();
        toReturn._totalNodes += nodes;
        return toReturn;
    }

    /**
     * Updates the maximum depth reached.
     * @param depth the current depth
     * @return a new DdoStats instance with updated maxExploredDepth
     */
    public DdoStats updateMaxDepth(int depth) {
        DdoStats toReturn = this.copy();
        toReturn._maxExploredDepth = Math.max(this._maxExploredDepth, depth);
        return toReturn;
    }

    /**
     * Updates the best lower bound and tracks its improvement iteration.
     * @param lb the new lower bound
     * @return a new DdoStats instance with updated bestLowerBound
     */
    public DdoStats updateLowerBound(double lb) {
        DdoStats toReturn = this.copy();
        if (lb > this._bestLowerBound) {
            toReturn._bestLowerBound = lb;
            toReturn._lastIterationOfLowerBoundImprovement = this._nbIterations;
        }
        return toReturn;
    }
}
