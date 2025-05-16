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

    public BPPState verboseInitialState(){
        return new BPPState(this, true);
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
        return new BPPState(this);
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(BPPState state, int var) {
        if(state.remainingItems == 0) return Collections.emptyIterator();
        ArrayList<Integer> suitableBin = new ArrayList<>();
        for (int i = 0; i < state.bins.size(); i++) {
            if (state.bins.get(i).remainingSpace() >= itemWeight[var]) suitableBin.add(i);
        }

        // By default, adding the possibility to add a new bin.
        suitableBin.add(state.bins.size());
        return suitableBin.iterator();
    }

    @Override
    public BPPState transition(BPPState state, Decision decision) {
        int item = decision.var();
        int bin = decision.val();
        int weight = itemWeight[item];
        BPPState newState = new BPPState(state);
        if(state.bins.size() == bin) newState.newBin();
        newState.packItem(item, weight, bin);
        return newState;
    }

    @Override
    public int transitionCost(BPPState state, Decision decision) {
        // If we are creating a new bin ==> -1 else 0
        if (decision.val() == state.bins.size()) return -1;
        else return 0;
    }
}
