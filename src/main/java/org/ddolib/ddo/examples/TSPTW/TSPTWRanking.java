package org.ddolib.ddo.examples.TSPTW;

import org.ddolib.ddo.heuristics.StateRanking;

public class TSPTWRanking implements StateRanking<TSPTWState> {
    @Override
    public int compare(TSPTWState o1, TSPTWState o2) {
        // In a layer,nodes with a non-empty possiblyVisit are children of a merged node.
        // There are good candidates to be merged.
        return -Integer.compare(o1.possiblyVisit().cardinality(), o2.possiblyVisit().cardinality());
    }
}
