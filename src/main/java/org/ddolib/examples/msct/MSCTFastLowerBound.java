package org.ddolib.examples.msct;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;

public class MSCTFastLowerBound implements FastLowerBound<MSCTState> {
    private final MSCTProblem problem;
    public MSCTFastLowerBound(MSCTProblem problem) {
        this.problem = problem;
    }
    @Override
    public double fastLowerBound(MSCTState state, Set<Integer> variables) {
        int k = variables.size();
        int minProcessing = Integer.MAX_VALUE;
        int minRelese = Integer.MAX_VALUE;
        for (Integer v : variables) {
            minProcessing = Math.min(minProcessing, problem.processing[v]);
            minRelese = Math.min(minRelese, problem.release[v]);
        }
        double u = Math.max(minRelese, state.currentTime());
        return  k * u + minProcessing * (k * (k+1) / 2.0);
    }
}
