package org.ddolib.ddo.examples.boundedknapsack;


import org.ddolib.ddo.implem.dominance.Dominance;

public class BKSDominance implements Dominance<Integer, Integer> {
    @Override
    public Integer getKey(Integer capa) {
        return 0;
    }

    @Override
    public boolean isDominatedOrEqual(Integer capa1, Integer capa2) {
        return capa1 < capa2;
    }
}
