package org.ddolib.examples.boundedknapsack;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.ArrayList;
import java.util.Iterator;

public class BKSProblem implements Problem<Integer> {

    final BKSInstance instance;

    public BKSProblem(BKSInstance instance) {
        this.instance = instance;
    }

    @Override
    public int nbVars() {
        return instance.values.length;
    }

    @Override
    public Integer initialState() {
        return instance.capacity;
    }

    @Override
    public double initialValue() {
        return 0.0;
    }

    @Override
    public Iterator<Integer> domain(Integer state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        domain.add(0);
        for (int v = 1; v <= instance.quantity[var]; v++) {
            if (state >= v * instance.weights[var]) {
                domain.add(v);
            }
        }
        return domain.iterator();
    }

    @Override
    public Integer transition(Integer state, Decision decision) {
        // If the item is taken (1), we decrease the capacity of the knapsack, otherwise leave it unchanged
        return state - instance.weights[decision.var()] * decision.val();
    }

    @Override
    public double transitionCost(Integer state, Decision decision) {
        // If the item is taken (1) the cost is the profit of the item, 0 otherwise
        return - instance.values[decision.var()] * decision.val();
    }
}
