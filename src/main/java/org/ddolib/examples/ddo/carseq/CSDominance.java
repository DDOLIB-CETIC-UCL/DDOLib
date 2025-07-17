package org.ddolib.examples.ddo.carseq;

import org.ddolib.modeling.Dominance;

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
