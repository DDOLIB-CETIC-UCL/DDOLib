package org.ddolib.examples.setcover.elementlayer;

import org.ddolib.modeling.StateRanking;

public class SetCoverRanking implements StateRanking<SetCoverState> {

    @Override
    public int compare(final SetCoverState o1, final SetCoverState o2) {
        return Integer.compare(o2.uncoveredElements.size(), o1.uncoveredElements.size());
    }
}