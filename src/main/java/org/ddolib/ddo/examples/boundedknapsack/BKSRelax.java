package org.ddolib.ddo.examples.boundedknapsack;


import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.Iterator;

public class BKSRelax implements Relaxation<Integer> {

    public BKSRelax() {
    }

    @Override
    public Integer mergeStates(final Iterator<Integer> states) {
        int capa = 0;
        while (states.hasNext()) {
            final Integer state = states.next();
            capa = Math.max(capa, state);
        }
        return capa;
    }

    @Override
    public double relaxEdge(Integer from, Integer to, Integer merged, Decision d, double cost) {
        return cost;
    }

}

