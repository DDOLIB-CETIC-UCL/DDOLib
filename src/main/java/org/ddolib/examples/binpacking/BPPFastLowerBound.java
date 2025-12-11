package org.ddolib.examples.binpacking;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;

public class BPPFastLowerBound implements FastLowerBound<BPPState> {

    private final BPPProblem problem;

    public BPPFastLowerBound(BPPProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(BPPState state, Set<Integer> variables) {
        if (variables.isEmpty()) return 0;
        int smallestRemainingItem = state.remainingItems.stream().max(Integer::compare).get();
        int remainingSpaceIfUsable = problem.itemWeight[smallestRemainingItem] <= state.remainingSpace ? state.remainingSpace : 0;

        return (int) Math.ceil((double) (state.remainingTotalWeight - remainingSpaceIfUsable) / problem.binMaxSpace);
    }
}
