package org.ddolib.examples.ddo.lcs;

import org.ddolib.modeling.StateRanking;

public class LCSRanking implements StateRanking<LCSState> {

    @Override
    public int compare(LCSState state1, LCSState state2) {
        // Best state is the one where the sum of string's position is the smallest.
        int totState1 = 0;
        int totState2 = 0;
        for (int i = 0; i < state1.position.length; i++) {
            totState1 += state1.position[i];
            totState2 += state2.position[i];
        }
        return Integer.compare(totState2, totState1);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
