package org.ddolib.examples.tsptw;

import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;

/**
 * Heuristic for computing the width of a layer in the dynamic programming model
 * for the Traveling Salesman Problem with Time Windows (TSPTW).
 * <p>
 * The width represents the maximum number of states to keep at a given layer.
 * It is computed based on the number of variables, the current depth, and a user-defined factor.
 * </p>
 */
public class TSPTWWidth implements WidthHeuristic<TSPTWState> {
    /** Number of variables/nodes in the TSPTW problem. */
    private final int nbVars;

    /** Factor to scale the width. */
    private final int factor;

    /**
     * Constructs a width heuristic for TSPTW layers.
     *
     * @param nbVars the number of variables/nodes in the problem
     * @param factor a scaling factor for the width calculation
     */
    public TSPTWWidth(int nbVars, int factor) {
        this.nbVars = nbVars;
        this.factor = factor;
    }
    /**
     * Computes the maximum width of a layer based on the current state.
     * <p>
     * The width is calculated as: (depth + 1) * nbVars * factor.
     * This allows the width to grow with the depth of the state in the DP model.
     * </p>
     *
     * @param state the state for which to compute the layer width
     * @return the maximum number of states to keep at this layer
     */
    @Override
    public int maximumWidth(TSPTWState state) {
        return (state.depth() + 1) * nbVars * factor;
    }
}
