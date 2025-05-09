package org.ddolib.ddo.examples.binpacking;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class BPPRelax implements Relaxation<BPPState> {

    BPPProblem problem;

    public BPPRelax(BPPProblem problem) {
        this.problem = problem;
    }

    @Override
    public BPPState mergeStates(Iterator<BPPState> states) {
        BPPState mergedState = new BPPState(states.next());
        while (states.hasNext()) {
            BPPState state = states.next();
            if (mergedState.bins.size() > state.bins.size()) {
                mergedState.bins = new ArrayList<>();
                mergedState.bins.addAll(state.bins);
            } else if (mergedState.bins.size() == state.bins.size()) {
                for (int i = 0; i < mergedState.bins.size(); i++) {
                    if(state.bins.get(i).remainingSpace() > mergedState.bins.get(i).remainingSpace()){
                        // meh
                        mergedState.bins.get(i).copy(state.bins.get(i));
                    }
                }
            }
        }
        return mergedState;
    }

    @Override
    public int relaxEdge(BPPState from, BPPState to, BPPState merged, Decision d, int cost) {
        return cost;
    }

    @Override
    public int fastUpperBound(BPPState state, Set<Integer> variables) {
        int totalRemainingSpace = state.bins.stream().map(Bin::remainingSpace).reduce(0, Integer::sum);
        int totalWeight = variables.stream().map(v -> problem.itemWeight[v]).reduce(0, Integer::sum);

        return -(int)Math.ceil((double)(totalWeight-totalRemainingSpace)/problem.binMaxSpace);
    }
}
