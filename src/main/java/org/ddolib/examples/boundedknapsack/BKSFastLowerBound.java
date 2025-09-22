package org.ddolib.examples.boundedknapsack;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;

public class BKSFastLowerBound implements FastLowerBound<Integer> {
    private final BKSProblem problem;

    public BKSFastLowerBound(BKSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(Integer state, Set<Integer> variables) {
        int rub = 0;
        for (int v : variables) {
            rub += this.problem.quantity[v] * this.problem.values[v];
        }
        return -rub;
    }
}
