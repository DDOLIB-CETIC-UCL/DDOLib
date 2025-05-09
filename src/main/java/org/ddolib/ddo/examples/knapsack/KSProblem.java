package org.ddolib.ddo.examples.knapsack;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class KSProblem implements Problem<Integer> {
    final int capa;
    final int[] profit;
    final int[] weight;
    public final Integer optimal;
    public KSProblem(final int capa, final int[] profit, final int[] weight, final Integer optimal) {
        this.capa = capa;
        this.profit = profit;
        this.weight = weight;
        this.optimal = optimal;
    }
    @Override
    public int nbVars() {
        return profit.length;
    }

    @Override
    public Integer initialState() {
        return capa;
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(Integer state, int var) {
        if (state >= weight[var]) {
            return Arrays.asList(1, 0).iterator();
        } else {
            return List.of(0).iterator();
        }
    }

    @Override
    public Integer transition(Integer state, Decision decision) {
        if (decision.val() == 1) {
            return state - weight[decision.var()];
        } else {
            return state;
        }
    }

    @Override
    public int transitionCost(Integer state, Decision decision) {
        return profit[decision.var()] * decision.val();
    }
}

