package org.ddolib.ddo.examples.knapsack;

import org.ddolib.ddo.heuristics.StateRanking;

public class KSRanking implements StateRanking<Integer>  {
    @Override
    public int compare(final Integer o1, final Integer o2) {
        return o1 - o2;
    }
}
