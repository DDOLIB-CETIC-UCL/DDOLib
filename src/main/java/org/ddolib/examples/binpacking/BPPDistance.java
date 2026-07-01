package org.ddolib.examples.binpacking;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;

import java.util.BitSet;

public class BPPDistance implements StateDistance<BPPState> {
    @Override
    public double distance(BPPState a, BPPState b) {
        BitSet xorBitSet = (BitSet) a.remainingItems().clone();
        xorBitSet.xor(b.remainingItems());
        return (double) xorBitSet.cardinality() /xorBitSet.size();
    }

    @Override
    public double distanceWithRoot(BPPState state) {
        return (double) state.remainingItems().cardinality() /state.remainingItems().size();
    }
}
