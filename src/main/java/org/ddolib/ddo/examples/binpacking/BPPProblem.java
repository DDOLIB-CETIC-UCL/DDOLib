package org.ddolib.ddo.examples.binpacking;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.*;

public class BPPProblem implements Problem<BPPState> {

    int nbItems;
    int binMaxSpace;
    int[] itemWeight;
    // Optimal solution
    Optional<Integer> optimal;

    BPPProblem(int nbItems, int binMaxSpace, int[] itemWeight, Optional<Integer> optimal) {
        this.nbItems = nbItems;
        this.binMaxSpace = binMaxSpace;
        this.itemWeight = itemWeight;
        this.optimal = optimal;
    }

    public Optional<Integer> getOptimal() {
        return optimal;
    }

    @Override
    public int nbVars() {
        return nbItems;
    }

    @Override
    public BPPState initialState() {
        HashSet<Integer> remainingItems = new HashSet<>();
        for(int i = 0; i<nbItems; i++) {
            remainingItems.add(i);
        }
        return new BPPState(binMaxSpace, remainingItems, 1);
    }

    @Override
    public double initialValue() {
        // Starting with one opened bin.
        return -1;
    }

    @Override
    public Iterator<Integer> domain(BPPState state, int var) {
        if (var >= nbVars()) return Collections.emptyIterator();

        // Filtering item that can still fit current bin.
        List<Integer> decisionsOnCurrent =
                state.remainingItems.stream().filter(item -> state.itemFitInBin(itemWeight[item])).toList();

        // If some items fit the current bin, use them, else add a new one.
        if (!decisionsOnCurrent.isEmpty())
            return decisionsOnCurrent.iterator();
        else
            return state.remainingItems.iterator();
    }

    @Override
    public BPPState transition(BPPState state, Decision decision) {
        int item = decision.val();
        int weight = itemWeight[item];
        BPPState newState;
        if (!state.itemFitInBin(weight)) {
            newState = state.newBin(binMaxSpace).packItem(item,weight);
        } else {
            newState = state.packItem(item, weight);
        }
        return newState;
    }

    @Override
    public double transitionCost(BPPState state, Decision decision) {
        int item = decision.val();
        int weight = itemWeight[item];
        // Not enough space ==> new bin ==> -1 else 0
        if (!state.itemFitInBin(weight)) return -1;
        else return 0;
    }
}


