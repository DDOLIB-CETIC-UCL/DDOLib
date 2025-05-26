package org.ddolib.ddo.examples.binpacking;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

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
    public int relaxEdge(BPPState from, BPPState to, BPPState merged, Decision d, int cost) {
        return -(merged.usedBins-from.usedBins);
    }

    @Override
    public int fastUpperBound(BPPState state, Set<Integer> variables) {

        if (variables.isEmpty()) return 0;
        int remainingTotalWeight = state.remainingItems.stream().map(i -> problem.itemWeight[i]).reduce(0,Integer::sum);
        int minRemainingSpace = state.remainingSpace;
        if(minRemainingSpace == -1)
            minRemainingSpace = state.remainingSpaces.stream().min(Integer::compareTo).orElse(0);

        int minBinsToOpen = (int) Math.ceil((double) (remainingTotalWeight - minRemainingSpace) / problem.binMaxSpace);

        return -minBinsToOpen;
    }
}
