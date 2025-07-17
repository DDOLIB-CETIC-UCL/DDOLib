package org.ddolib.ddo.examples.carseq;

import org.ddolib.ddo.implem.dominance.Dominance;

public class CSDominance implements Dominance<CSState, Integer> {
    private final CSProblem problem;

    public CSDominance(CSProblem problem) {
        this.problem = problem;
    }

    @Override
    public Integer getKey(CSState state) {
        return 0;
    }

    @Override
    public boolean isDominatedOrEqual(CSState state1, CSState state2) {
        return false;
    }
}
