package org.ddolib.examples.ddo.tsp;

import org.ddolib.modeling.StateRanking;

public class TSPRanking implements StateRanking<TSPState> {
    @Override
    public int compare(final TSPState o1, final TSPState o2) {
        return 0;
    }
}
