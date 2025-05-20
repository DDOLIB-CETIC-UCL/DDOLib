package org.ddolib.ddo.examples.binpacking;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.*;

public class BPPRelax implements Relaxation<BPPState> {

    BPPProblem problem;

    public BPPRelax(BPPProblem problem) {
        this.problem = problem;
    }

    @Override
    public BPPState mergeStates(Iterator<BPPState> states) {
        BPPState mergedState = new BPPState(states.next());
        int remainingItems = mergedState.remainingItems.size();
        HashSet<Integer> mergedRemainingItems = new HashSet<>(mergedState.remainingItems);
        while (states.hasNext()) {
            BPPState state = states.next();
            // If a state is using less bin or has more space in the current bin, take it.
            if (mergedState.remainingSpace() < state.remainingSpace() || mergedState.totalUsedBin() > state.totalUsedBin())
                mergedState = new BPPState(state);
            mergedRemainingItems.addAll(state.remainingItems);
        }
        int i = 0;
        mergedState.remainingItems.clear();
        List<Integer> listMergedRemainingItems = mergedRemainingItems.stream().toList();
        while(i < remainingItems){
            mergedState.remainingItems.add(listMergedRemainingItems.get(i));
            i++;
        }
        return mergedState;
    }

    @Override
    public int relaxEdge(BPPState from, BPPState to, BPPState merged, Decision d, int cost) {
        return cost;
    }

    @Override
    public int fastUpperBound(BPPState state, Set<Integer> variables) {
        if (variables.isEmpty()) return 0;
        int minBinsToOpen = (int) Math.ceil((double) (state.remainingTotalWeight - state.remainingSpace()) / problem.binMaxSpace);

        return -minBinsToOpen;
    }
}
