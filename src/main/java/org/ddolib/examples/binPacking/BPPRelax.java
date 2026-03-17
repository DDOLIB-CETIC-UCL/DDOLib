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

        BPPState first = states.next();
        int currentBinSpace = first.currentBinSpace();
        int usedBins = first.usedBins();
        BitSet remainingItems = (BitSet) first.remainingItems().clone();
        int nbRemainingItems = remainingItems.cardinality();

        while (states.hasNext()) {
            BPPState state = states.next();
            currentBinSpace = Math.max(state.currentBinSpace(), currentBinSpace);
            usedBins = Math.min(state.usedBins(), usedBins);
            remainingItems.or(state.remainingItems());
        }
        int nbItemsToIgnore = remainingItems.cardinality() - nbRemainingItems;
        for (int i = 0; i < nbItemsToIgnore; i++) {
            remainingItems.clear(remainingItems.nextSetBit(0));
        }

        return new BPPState(currentBinSpace, usedBins, remainingItems);
    }

    @Override
    public double relaxEdge(BPPState from, BPPState to, BPPState merged, Decision d, double cost) {
        return cost;
    }
}
