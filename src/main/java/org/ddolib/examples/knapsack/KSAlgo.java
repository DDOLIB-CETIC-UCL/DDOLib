package org.ddolib.examples.knapsack;

import java.util.Arrays;

/**
 * Utility class providing algorithms to solve the Knapsack Problem.
 */
public class KSAlgo {

    /**
     * Provides a greedy approximation for the Knapsack Problem.
     *
     * <p>The algorithm sorts items in descending order of their profit-to-weight ratio
     * ({@code profit / weight}) and selects items sequentially as long as they fit 
     * within the remaining capacity.</p>
     *
     * @param problem the Knapsack Problem instance to solve
     * @return the total profit of the selected items, providing a primal bound (lower bound)
     *         on the optimal solution
     */
    public static int greedyKS(KSProblem problem) {
        Integer[] items = new Integer[problem.nbVars()];
        for (int i = 0; i < problem.nbVars(); i++) {
            items[i] = i;
        }

        Arrays.sort(items, (o1, o2) -> {
            double ratio1 = (double) problem.profit[o1] / problem.weight[o1];
            double ratio2 = (double) problem.profit[o2] / problem.weight[o2];
            return Double.compare(ratio2, ratio1);
        });

        int capa = problem.capa;
        int value = 0;
        for (Integer i : items) {
            if (capa < problem.weight[i]) continue;
            value += problem.profit[i];
            capa -= problem.weight[i];
        }

        return value;
    }
}
