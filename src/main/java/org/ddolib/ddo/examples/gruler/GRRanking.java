package org.ddolib.ddo.examples.gruler;

import org.ddolib.modeling.StateRanking;

public class GRRanking implements StateRanking<GRState> {
    @Override
    public int compare(GRState s1, GRState s2) {
        return Integer.compare(s1.getLastMark(), s2.getLastMark()); // sort by last mark
    }
}
