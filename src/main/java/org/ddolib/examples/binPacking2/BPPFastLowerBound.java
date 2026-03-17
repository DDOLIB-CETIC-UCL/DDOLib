package org.ddolib.examples.binPacking2;

import org.ddolib.modeling.FastLowerBound;
import org.ddolib.util.algo.BinPacking;

import java.util.Arrays;
import java.util.Collections;
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
        fullItems[0] = problem.binMaxSpace - state.currentBinSpace;

        int fullItemId = 1;
        for (int i = state.remainingItems.nextSetBit(0); i >= 0; i = state.remainingItems.nextSetBit(i + 1)) {
            fullItems[fullItemId] = problem.itemWeights[i];
            fullItemId++;
        }
        // Sort only first element
        fullItemId = 1;
        while(fullItemId < fullItems.length && fullItems[fullItemId] > fullItems[fullItemId - 1]) {
            int temp = fullItems[fullItemId];
            fullItems[fullItemId] = fullItems[fullItemId - 1];
            fullItems[fullItemId - 1] = temp;
            fullItemId++;
        }
        //System.out.println(state.usedBins + Math.max(0,BinPacking.labbeLB(fullItems, this.problem.binMaxSpace) - 1));
        return Math.max(0,BinPacking.labbeLB(fullItems, this.problem.binMaxSpace) - 1);
    }
}
