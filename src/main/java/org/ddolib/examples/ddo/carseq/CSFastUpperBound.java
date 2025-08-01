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
        for (int i = 0; i < problem.nOptions(); i++) { // Look at each option separately
            int k = problem.blockMax[i], l = problem.blockSize[i], n = state.nToBuild;
            int max = n / l * k + // Max number of cars that wouldn't cause violations
                Math.max(0, Math.min(n % l, problem.blockMax[i] - Long.bitCount(state.previousBlocks[i] & ((1L << (l - n % l)) - 1))));
            if (state.nWithOption[i] > max) { // Too many cars remaining with that option -> there will be violations
                bound -= state.nWithOption[i] - max;
            }
        }
        return bound;
    }
}
