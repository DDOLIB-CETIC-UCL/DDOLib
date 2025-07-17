package org.ddolib.example.ddo.pigmentscheduling;

import org.ddolib.modeling.StateRanking;

import java.util.Arrays;

public class PSRanking implements StateRanking<PSState> {
    @Override
    public int compare(PSState s1, PSState s2) {
        // the state with the smallest total demand is the best (not sure about this)
        int totS1 = Arrays.stream(s1.previousDemands).sum();
        int totS2 = Arrays.stream(s2.previousDemands).sum();
        return Integer.compare(totS1, totS2);
    }
}
