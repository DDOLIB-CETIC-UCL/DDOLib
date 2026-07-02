package org.ddolib.layered.examples.pdptw;

import org.ddolib.layered.modeling.StateRanking;

/**
 * Neutral ranking heuristic for PDPTW states.
 */
public class PDPTWRanking implements StateRanking<PDPTWState> {
    @Override
    public int compare(final PDPTWState o1, final PDPTWState o2) {
        return 0;
    }
}
