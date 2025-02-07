package org.ddolib.ddo.examples.max2sat;

import org.ddolib.ddo.heuristics.StateRanking;

public class Max2SatRanking implements StateRanking<Max2SatState> {


    @Override
    public int compare(Max2SatState o1, Max2SatState o2) {
        return Integer.compare(o1.rank(), o2.rank());
    }
}
