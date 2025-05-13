package org.ddolib.ddo.examples.tsptw;

import org.ddolib.ddo.implem.dominance.Dominance;

import java.util.BitSet;

/**
 * Dominance class for the TSPTW problem.
 * If for two states, the position is the same and the must visit set is the same,
 * then we can only keep the state with the lowest time.
 */
public class TSPTWDominance implements Dominance<TSPTWState,TSPTWDominanceKey> {

    @Override
    public TSPTWDominanceKey getKey(TSPTWState state) {
        return new TSPTWDominanceKey(state.position(),state.mustVisit());
    }

    @Override
    public boolean isDominatedOrEqual(TSPTWState state1, TSPTWState state2) {
        return state1.time() >= state2.time();
    }
}

record TSPTWDominanceKey(Position p, BitSet mustVisit) {
    @Override
    public String toString() {
        return String.format("position: %s - must visit: %s",
                p,
                mustVisit);
    }
}