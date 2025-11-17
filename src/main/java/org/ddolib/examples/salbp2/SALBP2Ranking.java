package org.ddolib.examples.salbp2;

import org.ddolib.modeling.StateRanking;

public class SALBP2Ranking implements StateRanking<SALBP2State> {
    @Override
    public int compare(SALBP2State s1, SALBP2State s2) {
        return Double.compare(s1.cycle(), s2.cycle());
    }
}
// 05 au 16
