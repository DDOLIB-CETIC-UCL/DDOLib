package org.ddolib.example.ddo.boundedknapsack;

import org.ddolib.modeling.StateRanking;

public class BKSRanking implements StateRanking<Integer> {
    @Override
    public int compare(final Integer o1, final Integer o2) {
        return o1 - o2;
    }
}
