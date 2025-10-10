package org.ddolib.examples.knapsack;

import org.ddolib.modeling.Dominance;

public class KSDominance implements Dominance<Integer> {

    @Override
    public Integer getKey(Integer capa) {
        return 0;
    }

    @Override
    public boolean isDominatedOrEqual(Integer capa1, Integer capa2) {
        return capa1 <= capa2;
    }
}