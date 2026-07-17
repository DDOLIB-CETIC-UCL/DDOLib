package org.ddolib.examples.layered.pdptw;

import org.ddolib.layered.modeling.StateRanking;

/**
 * Neutral ranking heuristic for PDPTW states.
 */
public class PDPTWRanking implements StateRanking<PDPTWState> {

    /**
     * Creates a new instance of this ranking.
     */
    public PDPTWRanking() {
    }

    @Override
    public int compare(final PDPTWState o1, final PDPTWState o2) {
        return 0;
    }
}
