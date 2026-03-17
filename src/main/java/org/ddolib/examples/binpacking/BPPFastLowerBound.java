package org.ddolib.examples.binpacking;

import org.ddolib.modeling.FastLowerBound;
import org.ddolib.util.algo.BinPacking;

import java.util.Arrays;
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
        int usedBins = state.usedBins();
        int[] fullItems = new int[usedBins + variables.size()];

        System.arraycopy(state.binsUsedSpace, 0, fullItems, 0, usedBins);
        Arrays.sort(fullItems, 0, usedBins);

        for (int left = 0, right = usedBins - 1; left < right; left++, right--) {
            int temp = fullItems[left];
            fullItems[left] = fullItems[right];
            fullItems[right] = temp;
        }

        int currentIdx = usedBins;
        for (Integer val : variables) {
            fullItems[currentIdx++] = val;
        }
        return Math.max(0,BinPacking.labbeLB(fullItems, this.problem.binMaxSpace) - state.usedBins());
    }
}
