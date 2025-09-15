package org.ddolib.examples.setcover.setlayer;

import org.ddolib.modeling.StateRanking;

public class SetCoverRanking implements StateRanking<SetCoverState> {

    @Override
    public int compare(final SetCoverState o1, final SetCoverState o2) {
        return Integer.compare(o1.size(), o2.size());
    }
}