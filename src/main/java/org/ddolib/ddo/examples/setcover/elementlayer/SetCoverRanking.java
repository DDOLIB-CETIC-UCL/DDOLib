package org.ddolib.ddo.examples.setcover.elementlayer;

import org.ddolib.ddo.heuristics.StateRanking;

public class SetCoverRanking implements StateRanking<SetCoverState> {

    @Override
    public int compare(final SetCoverState o1, final SetCoverState o2) {
        return Integer.compare(o2.uncoveredElements.size(), o1.uncoveredElements.size());
    }
}