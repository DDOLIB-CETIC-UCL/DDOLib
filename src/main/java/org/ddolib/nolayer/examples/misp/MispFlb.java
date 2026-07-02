package org.ddolib.nolayer.examples.misp;

import org.ddolib.nolayer.modeling.FastLowerBound;

public class MispFlb implements FastLowerBound<MispState> {

    private final int[] weight;

    public MispFlb(MispProblem problem) {
        this.weight = problem.weight;
    }

    @Override
    public double fastLowerBound(MispState state) {
        double flb = 0;
        for (int i = state.remainingNodes().nextSetBit(0); i >= 0; i = state.remainingNodes().nextSetBit(i + 1)) {
            flb += weight[i];
        }
        return flb;
    }
}
