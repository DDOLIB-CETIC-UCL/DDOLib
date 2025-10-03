package org.ddolib.examples.tsptw;

import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;

/**
 * Compute the width of a layer based on the number of variables, the depth and a given factor.
 */
public class TSPTWWidth implements WidthHeuristic<TSPTWState> {
    private final int nbVars;
    private final int factor;

    public TSPTWWidth(int nbVars, int factor) {
        this.nbVars = nbVars;
        this.factor = factor;
    }

    @Override
    public int maximumWidth(TSPTWState state) {
        return (state.depth() + 1) * nbVars * factor;
    }
}
