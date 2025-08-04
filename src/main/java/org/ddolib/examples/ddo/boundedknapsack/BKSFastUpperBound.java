package org.ddolib.examples.ddo.boundedknapsack;

import org.ddolib.modeling.FastUpperBound;

import java.util.Set;

public class BKSFastUpperBound implements FastUpperBound<Integer> {
    private final BKSProblem problem;

    public BKSFastUpperBound(BKSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastUpperBound(Integer state, Set<Integer> variables) {
        int rub = 0;
        for (int v : variables) {
            rub += this.problem.quantity[v] * this.problem.values[v];
        }
        return rub;
    }
}
