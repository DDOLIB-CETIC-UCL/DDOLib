package org.ddolib.examples.gruler;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;

public class GRFastLowerBound implements FastLowerBound<GRState> {
    final GRProblem problem;
    public GRFastLowerBound(GRProblem problem) {
        this.problem = problem;
    }
    @Override
    public double fastLowerBound(GRState state, Set<Integer> variables) {
        return 0.0;
    }
}
