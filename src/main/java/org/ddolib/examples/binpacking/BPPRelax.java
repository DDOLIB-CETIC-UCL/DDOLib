package org.ddolib.examples.binpacking;

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

        // Take the biggest current bin space.
        // Take the smallest last remaining space (ordering bin from fullest to emptiest).
        // Take the union of remaining items in all the states then remove the heaviest ones.

        BPPState first = states.next();
        int currentBinSpace = first.currentBinSpace();
        BitSet remainingItems = (BitSet) first.remainingItems().clone();
        int lastRemainingSpace = first.lastRemainingSpace();

        while (states.hasNext()) {
            BPPState state = states.next();
            currentBinSpace = Math.max(state.currentBinSpace(), currentBinSpace);
            remainingItems.or(state.remainingItems());
            lastRemainingSpace = Math.min(state.lastRemainingSpace(), lastRemainingSpace);
        }

        return new BPPState(currentBinSpace, remainingItems, lastRemainingSpace);
    }

    @Override
    public double relaxEdge(BPPState from, BPPState to, BPPState merged, Decision d, double cost) {
        return cost;
    }
}
