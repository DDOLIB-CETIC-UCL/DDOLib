package org.ddolib.examples.ddo.knapsack;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.Iterator;

public class KSRelax implements Relaxation<Integer> {

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
