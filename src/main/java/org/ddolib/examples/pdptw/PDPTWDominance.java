package org.ddolib.examples.pdptw;

import org.ddolib.modeling.Dominance;

/**
 * Dominance class for the TSPTW problem.
 * If for two states, the position is the same and the {@code mustVisit} set is the same,
 * then we can only keep the state with the lowest time.
 */
public class PDPTWDominance implements Dominance<PDPTWState, PDPTWDominanceKey> {

    @Override
    public PDPTWDominanceKey getKey(PDPTWState state) {
        return new PDPTWDominanceKey(state.openToVisit, state.allToVisit, state.current,
                state.minContent, state.maxContent);
    }

    @Override
    public boolean isDominatedOrEqual(PDPTWState state1, PDPTWState state2) {
        return state1.currentTime >= state2.currentTime;
    }
}

