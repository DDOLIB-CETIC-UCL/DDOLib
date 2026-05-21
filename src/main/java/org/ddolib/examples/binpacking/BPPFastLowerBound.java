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

        // To use labbeLB algorithm, we cannot consider bin that are partially full.
        // Therefore, we remove the content of each used bin AS ONE ITEM and add it to the list (sorted biggest to lowest).
        // And we run the algorithm. We need to remove all used bins from the result (otherwise counted twice).
        int[] fullItems = new int[1 + variables.size()];
        fullItems[0] = problem.binMaxSpace - state.currentBinSpace();

        // We may have way more items than necessary (due to relaxation)
        // We keep the smallest items ordered in decreasing order.
        int setBit = state.remainingItems().previousSetBit(state.remainingItems().size());
        for (int i = fullItems.length - 1; i > 0; i--) {
            fullItems[i] = problem.itemWeights[setBit];
            setBit = state.remainingItems().previousSetBit(setBit - 1);
        }
        // Sort only first element to place it at the right spot.
        int fullItemId = 1;
        while (fullItemId < fullItems.length && fullItems[fullItemId] > fullItems[fullItemId - 1]) {
            int temp = fullItems[fullItemId];
            fullItems[fullItemId] = fullItems[fullItemId - 1];
            fullItems[fullItemId - 1] = temp;
            fullItemId++;
        }
        return Math.max(0, BinPacking.labbeLB(fullItems, this.problem.binMaxSpace) - 1);
    }
}
