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
        int minUsedBin = Integer.MAX_VALUE;

        HashSet<Integer> newRemainingSpaces = new HashSet<>();
        HashSet<Integer> newRemainingItems = new HashSet<>();
        while (states.hasNext()) {
            BPPState state = states.next();
            if(remainingItemToPack == 0)
                remainingItemToPack = state.remainingItems.size();
            if(state.remainingSpace == -1)
                newRemainingSpaces.addAll(state.remainingSpaces);
            else
                newRemainingSpaces.add(state.remainingSpace);
            newRemainingItems.addAll(state.remainingItems);
            minUsedBin = Math.min(minUsedBin, state.usedBins);
        }
        newRemainingItems = newRemainingItems.stream().sorted(weightComparator).limit(remainingItemToPack).collect(Collectors.toCollection(HashSet::new));
        return new BPPState(newRemainingSpaces, newRemainingItems, minUsedBin);
    }

    @Override
    public double relaxEdge(BPPState from, BPPState to, BPPState merged, Decision d, double cost) {
        return -(merged.usedBins-from.usedBins);
    }
}
