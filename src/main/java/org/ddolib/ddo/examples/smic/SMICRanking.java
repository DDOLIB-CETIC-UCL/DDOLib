package org.ddolib.ddo.examples.smic;

import org.ddolib.modeling.StateRanking;

public class SMICRanking implements StateRanking<SMICState> {
    @Override
    public int compare(SMICState o1, SMICState o2) {
        return Integer.compare(o1.getCurrentTime(), o2.getCurrentTime());
    }
}



