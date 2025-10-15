package org.ddolib.examples.boundedknapsack;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class BKSProblem implements Problem<Integer> {
    public final int capacity;
    public final int[] values;
    public final int[] weights;
    public final int[] quantity;
//    final BKSInstance instance;

    public BKSProblem(int capacity, int[] values, int[] weights, int[] quantity) {
        this.capacity = capacity;
        this.values = values;
        this.weights = weights;
        this.quantity = quantity;
    }

    public BKSProblem(int n, int Range, InstanceType type, long seed) {
        Random rand = new Random(seed);

        int[] values = new int[n];
        int[] weights = new int[n];
        int[] quantity = new int[n];
        int totalWeight = 0;
        for (int j = 0; j < n; j++) {
            int w = 1 + rand.nextInt(Range);
            int p;
            switch (type) {
                case UNCORRELATED:
                    p = 1 + rand.nextInt(Range);
                    break;
                case WEAKLY_CORRELATED:
                    int minP = Math.max(1, w - Range / 10);
                    int maxP = Math.min(Range, w + Range / 10);
                    p = minP + rand.nextInt(maxP - minP + 1);
                    break;
                case STRONGLY_CORRELATED:
                    p = w + 10;
                    break;
                case SUBSET_SUM:
                    p = w;
                    break;
                default:
                    p = 1;
            }
            int m = 5 + rand.nextInt(6);
            values[j] = p;
            weights[j] = w;
            quantity[j] = m;
            totalWeight += w * m;
        }
        int capacity = (int) (0.5 * totalWeight); // capacity = 50% of total
        this.capacity = capacity;
        this.values = values;
        this.weights = weights;
        this.quantity = quantity;
    }

    @Override
    public int nbVars() {
        return values.length;
    }

    @Override
    public Integer initialState() {
        return capacity;
    }

    @Override
    public double initialValue() {
        return 0.0;
    }

    @Override
    public Iterator<Integer> domain(Integer state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        domain.add(0);
        for (int v = 1; v <= quantity[var]; v++) {
            if (state >= v * weights[var]) {
                domain.add(v);
            }
        }
        return domain.iterator();
    }

    @Override
    public Integer transition(Integer state, Decision decision) {
        // If the item is taken (1), we decrease the capacity of the knapsack, otherwise leave it unchanged
        return state - weights[decision.var()] * decision.val();
    }

    @Override
    public double transitionCost(Integer state, Decision decision) {
        // If the item is taken (1) the cost is the profit of the item, 0 otherwise
        return - values[decision.var()] * decision.val();
    }

    enum InstanceType {
        UNCORRELATED, // Profit and weight are independent
        WEAKLY_CORRELATED, //Profits roughly follow weights, but not exactly
        STRONGLY_CORRELATED, // Profit = weight + constant
        SUBSET_SUM // Profits exactly equal weights
    }
}
