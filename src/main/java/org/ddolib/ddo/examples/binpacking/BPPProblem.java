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

    public BPPState verboseInitialState() {
        return new BPPState(this, true);
    }

    public Optional<Integer> getOptimal() {
        return optimal;
    }

    /**
     * Formats the BPPDecision as an Integer.
     *
     * @param decision The decision.
     * @return The formated decision.
     */
    public int toDecision(BPPDecision decision) {
        return decision.item + nbItems * decision.bin;
    }

    /**
     * Restores a decision object from its Integer form.
     *
     * @param value The formatted Integer form of the decision.
     * @return An BPPDecision object.
     */
    public BPPDecision fromDecision(int value) {
        return new BPPDecision(
                value % nbItems,
                value / nbItems
        );
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
        // Starting with one opened bin.
        return -1;
    }

    @Override
    public Iterator<Integer> domain(BPPState state, int var) {
        if (var >= nbVars()) return Collections.emptyIterator();

        // Filtering item that can still fit current bin.
        List<Integer> decisionsOnCurrent =
                state.remainingItems.stream().filter(item -> itemWeight[item] <= state.remainingSpace()).
                        map(item -> toDecision(new BPPDecision(item, state.currentBinId()))).toList();

        // If some items fit the current bin, use them, else add a new one.
        if (!decisionsOnCurrent.isEmpty())
            return decisionsOnCurrent.iterator();
        else
            return state.remainingItems.stream().map(item -> toDecision(new BPPDecision(item, state.currentBinId()+1))).iterator();
    }

    @Override
    public BPPState transition(BPPState state, Decision decision) {
        BPPDecision bppDecision = fromDecision(decision.val());
        int bin = bppDecision.bin;
        int item = bppDecision.item;
        int weight = itemWeight[item];
        BPPState newState = new BPPState(state);
        if (state.currentBinId() != bppDecision.bin) newState.newBin();
        newState.packItem(item, weight, bin);
        return newState;
    }

    @Override
    public int transitionCost(BPPState state, Decision decision) {
        // If we are creating a new bin ==> -1 else 0
        if (fromDecision(decision.val()).bin != state.currentBinId()) return -1;
        else return 0;
    }
}


