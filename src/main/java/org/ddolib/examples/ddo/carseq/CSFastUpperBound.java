package org.ddolib.examples.ddo.carseq;

import org.ddolib.modeling.FastUpperBound;

import java.util.Set;

public class CSFastUpperBound implements FastUpperBound<CSState> {
    private final CSProblem problem;

    public CSFastUpperBound(CSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastUpperBound(CSState state, Set<Integer> variables) {
        double bound = 0;
        for (int i = 0; i < problem.nOptions(); i++) {
            int k = problem.blockMax[i], l = problem.blockSize[i], n = state.nToBuild + l;
            int max = n / l * k + Math.min(k, n % l);
            if (state.nWithOption[i] > max) {
                bound -= state.nWithOption[i] - max;
            }
        }
        return bound;
    }
}
