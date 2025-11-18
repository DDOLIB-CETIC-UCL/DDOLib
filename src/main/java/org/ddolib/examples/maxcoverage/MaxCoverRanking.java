package org.ddolib.examples.maxcoverage;

import org.ddolib.modeling.StateRanking;

public class MaxCoverRanking implements StateRanking<MaxCoverState> {
    @Override
    public int compare(MaxCoverState o1, MaxCoverState o2) {
        return Integer.compare(o1.coveredItems().size(), o2.coveredItems().size());
    }
}
