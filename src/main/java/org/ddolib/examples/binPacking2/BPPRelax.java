package org.ddolib.examples.binPacking2;

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

        int nCurrentBinSpace = Integer.MIN_VALUE;
        int nUsedBins = Integer.MAX_VALUE;
        BitSet nRemainingItems = new BitSet(problem.nbItems);
        nRemainingItems.set(0, problem.nbItems);
        int nbRemainingItems = -1;

        while (states.hasNext()) {
            BPPState state = states.next();
            if(nbRemainingItems == -1) { nbRemainingItems = state.remainingItems.cardinality(); }
            nCurrentBinSpace = Math.max(state.currentBinSpace, nCurrentBinSpace);
            nUsedBins = Math.min(state.usedBins, nUsedBins);
            nRemainingItems.or(state.remainingItems);
        }
        int nbItemsToIgnore = nRemainingItems.cardinality() - nbRemainingItems;
        for (int i = 0; i < nbItemsToIgnore; i++) {
            nRemainingItems.clear(nRemainingItems.nextSetBit(0));
        }

        return new BPPState(nCurrentBinSpace,nUsedBins,problem,nRemainingItems);
    }

    @Override
    public double relaxEdge(BPPState from, BPPState to, BPPState merged, Decision d, double cost) {
        return cost;
    }
}
