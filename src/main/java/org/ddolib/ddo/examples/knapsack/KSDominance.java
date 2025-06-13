package org.ddolib.ddo.examples.knapsack;

import org.ddolib.ddo.implem.dominance.Dominance;

class KSDominance implements Dominance<Integer, Integer> {

    @Override
    public Integer getKey(Integer capa) {
        return 0;
    }

    @Override
    public boolean isDominatedOrEqual(Integer capa1, Integer capa2) {
        return capa1 <= capa2;
    }
}