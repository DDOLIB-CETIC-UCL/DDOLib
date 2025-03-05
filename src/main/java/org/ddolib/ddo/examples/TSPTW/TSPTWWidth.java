package org.ddolib.ddo.examples.TSPTW;

import org.ddolib.ddo.heuristics.WidthHeuristic;

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
