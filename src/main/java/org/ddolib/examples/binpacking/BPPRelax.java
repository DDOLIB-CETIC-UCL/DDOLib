package org.ddolib.examples.binpacking;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.*;
import java.util.stream.Collectors;

public class BPPRelax implements Relaxation<BPPState> {

    BPPProblem problem;

    public BPPRelax(BPPProblem problem) {
        this.problem = problem;
    }

    @Override
    public BPPState mergeStates(Iterator<BPPState> states) {
        Comparator<Integer> weightComparator = (o1, o2) -> Integer.compare(problem.itemWeight[o1],problem.itemWeight[o2]);

        int remainingItemToPack = 0;
        int newRemainingSpace = Integer.MIN_VALUE;
        int minUsedBin = Integer.MAX_VALUE;
        int newWastedSpace = Integer.MAX_VALUE;

        HashSet<Integer> newRemainingItems = new HashSet<>();
        while (states.hasNext()) {
            BPPState state = states.next();
            if(remainingItemToPack == 0)
                remainingItemToPack = state.remainingItems.size();
            newRemainingSpace = Math.max(newRemainingSpace, state.remainingSpace);
            newRemainingItems.addAll(state.remainingItems);
            minUsedBin = Math.min(minUsedBin, state.usedBins);
            newWastedSpace = Math.min(newWastedSpace, state.wastedSpace);
        }
        newRemainingItems = newRemainingItems.stream().sorted(weightComparator).limit(remainingItemToPack).collect(Collectors.toCollection(HashSet::new));
        int newRemainingTotalWeight = newRemainingItems.stream().reduce(0, Integer::sum);
        return new BPPState(newRemainingSpace,newRemainingItems,minUsedBin,newRemainingTotalWeight,newWastedSpace);
    }

    @Override
    public double relaxEdge(BPPState from, BPPState to, BPPState merged, Decision d, double cost) {
        return (merged.usedBins-from.usedBins);
    }
}
