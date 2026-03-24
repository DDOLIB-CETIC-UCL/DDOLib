package org.ddolib.examples.binPacking;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;

public class BPPRelax implements Relaxation<BPPState> {

    BPPProblem problem;

    public BPPRelax(BPPProblem problem) {
        this.problem = problem;
    }

    @Override
    public BPPState mergeStates(Iterator<BPPState> states) {

        // take the min number of used bins in all the states
        // take the max current bin space in all the states
        // take the union of remaining items in all the states then remove the heaviest ones

        BPPState first = states.next();
        int currentBinSpace = first.currentBinSpace();
        BitSet remainingItems = (BitSet) first.remainingItems().clone();
        int nbRemainingItems = remainingItems.cardinality();
        int lastRemainingSpace = first.lastRemainingSpace();

        while (states.hasNext()) {
            BPPState state = states.next();
            assert(nbRemainingItems == state.remainingItems().cardinality()); // assume to merge states of a same layer
            currentBinSpace = Math.max(state.currentBinSpace(), currentBinSpace);
            remainingItems.or(state.remainingItems());
            lastRemainingSpace = Math.min(state.lastRemainingSpace(), lastRemainingSpace);
        }
        // delete the heaviest items until we have nbRemainingItems items in the merged state
        int nbItemsToIgnore = remainingItems.cardinality() - nbRemainingItems;
        for (int i = 0; i < nbItemsToIgnore; i++) {
            remainingItems.clear(remainingItems.nextSetBit(0));
        }

        return new BPPState(currentBinSpace, remainingItems, lastRemainingSpace);
    }

    @Override
    public double relaxEdge(BPPState from, BPPState to, BPPState merged, Decision d, double cost) {
        return cost;
    }
}
