package org.ddolib.ddo.examples.tsptw;

import org.ddolib.ddo.modeling.Dominance;

/**
 * Dominance class for the TSPTW problem.
 * If for two states, the position is the same and the {@code mustVisit} set is the same,
 * then we can only keep the state with the lowest time.
 */
public class TSPTWDominance implements Dominance<TSPTWState, TSPTWDominanceKey> {

    @Override
    public TSPTWDominanceKey getKey(TSPTWState state) {
        return new TSPTWDominanceKey(state.position(), state.mustVisit());
    }

    @Override
    public boolean isDominatedOrEqual(TSPTWState state1, TSPTWState state2) {
        return state1.time() >= state2.time();
    }
}

