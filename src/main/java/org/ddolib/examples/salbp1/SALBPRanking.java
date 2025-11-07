package org.ddolib.examples.salbp1;

import org.ddolib.modeling.StateRanking;

public class SALBPRanking implements StateRanking<SALBPState> {
    @Override
    public int compare(SALBPState s1, SALBPState s2) {
        return Integer.compare(s1.stations().size(), s2.stations().size());
    }
}
