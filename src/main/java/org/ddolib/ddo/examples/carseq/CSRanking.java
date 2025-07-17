package org.ddolib.ddo.examples.carseq;

import org.ddolib.ddo.heuristics.StateRanking;

public class CSRanking implements StateRanking<CSState> {
    private final CSProblem problem;

    public CSRanking(CSProblem problem) {
        this.problem = problem;
    }

    @Override
    public int compare(final CSState state1, final CSState state2) {
        return state2.lowerBound - state1.lowerBound;
    }
}
