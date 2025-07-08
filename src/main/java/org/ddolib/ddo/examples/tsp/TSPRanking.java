package org.ddolib.ddo.examples.tsp;

import org.ddolib.ddo.heuristics.StateRanking;

public class TSPRanking implements StateRanking<TSPState> {
    @Override
    public int compare(final TSPState o1, final TSPState o2) {
        return 0;
    }
}
