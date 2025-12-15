package org.ddolib.examples.binpacking;

import org.ddolib.modeling.FastLowerBound;
import org.ddolib.util.algo.BinPacking;

import java.util.*;

public class BPPFastLowerBound implements FastLowerBound<BPPState> {

    private final BPPProblem problem;

    public BPPFastLowerBound(BPPProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(BPPState state, Set<Integer> variables) {
        if (variables.isEmpty()) return 0;

        int currentBinSpace = state.remainingSpace;
        Iterator<Integer> remainingItemWeightsIt = state.remainingItems.stream().map(i -> problem.itemWeight[i]).iterator();
        ArrayList<Integer> remainingItemWeightsList = new ArrayList<>();
        while (remainingItemWeightsIt.hasNext()) {
            int itemWeight = remainingItemWeightsIt.next();
            if(itemWeight < currentBinSpace) currentBinSpace -= itemWeight;
            else remainingItemWeightsList.add(itemWeight);
        }
        int[] remainingItemWeights = remainingItemWeightsList.stream().mapToInt(Integer::intValue).toArray();

        return BinPacking.labbeLB(remainingItemWeights, this.problem.binMaxSpace);
    }
}
