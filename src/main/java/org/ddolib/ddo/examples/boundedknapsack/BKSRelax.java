package org.ddolib.ddo.examples.boundedknapsack;


import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.Iterator;
import java.util.Set;

public class BKSRelax implements Relaxation<Integer> {

    private final BKSProblem problem;

    public BKSRelax(BKSProblem problem) {
        this.problem = problem;
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
    public int relaxEdge(Integer from, Integer to, Integer merged, Decision d, int cost) {
        return cost;
    }

    @Override
    public int fastUpperBound(Integer state, Set<Integer> variables) {
        int rub = 0;
        for (int v : variables) {
            rub += this.problem.quantity[v] * this.problem.values[v];
        }
        return rub;
    }
}

