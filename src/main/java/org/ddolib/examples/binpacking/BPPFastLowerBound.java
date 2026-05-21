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

        int setBit = state.remainingItems().nextSetBit(0);
        for (int i = 1; i < fullItems.length; i++) {
            fullItems[i] = problem.itemWeights[setBit];
            setBit = state.remainingItems().nextSetBit(setBit + 1);
        }
        // Sort only first element to place it at the right spot.
        // (since all others are already sorted)
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
