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
        int remainingTotalWeight = state.remainingItems.stream().map(i -> problem.itemWeight[i]).reduce(0,Integer::sum);
        int minRemainingSpace = state.remainingSpace;
        if(minRemainingSpace == -1)
            minRemainingSpace = state.remainingSpaces.stream().min(Integer::compareTo).orElse(0);

        return (int) Math.ceil((double) (remainingTotalWeight - minRemainingSpace) / problem.binMaxSpace);
    }
}
