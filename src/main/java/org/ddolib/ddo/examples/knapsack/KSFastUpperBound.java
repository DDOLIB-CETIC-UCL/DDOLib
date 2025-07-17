package org.ddolib.ddo.examples.knapsack;

import org.ddolib.modeling.FastUpperBound;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

/**
 * Implementation of a fast upper bound heuristic for the Knapsack problem.
 */
public class KSFastUpperBound implements FastUpperBound<Integer> {
    private final KSProblem problem;

    /**
     * Instantiates the class.
     *
     * @param problem The associated Knapsack instance.
     */
    public KSFastUpperBound(KSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastUpperBound(Integer state, Set<Integer> variables) {
        double[] ratio = new double[problem.nbVars()];
        int capacity = state;
        for (int v : variables) {
            ratio[v] = ((double) problem.profit[v] / problem.weight[v]);
        }

        class RatioComparator implements Comparator<Integer> {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Double.compare(ratio[o1], ratio[o2]);
            }
        }

        Integer[] sorted = variables.toArray(new Integer[0]);
        Arrays.sort(sorted, new RatioComparator().reversed());

        int maxProfit = 0;
        Iterator<Integer> itemIterator = Arrays.stream(sorted).iterator();
        while (capacity > 0 && itemIterator.hasNext()) {
            int item = itemIterator.next();
            if (capacity >= problem.weight[item]) {
                maxProfit += problem.profit[item];
                capacity -= problem.weight[item];
            } else {
                double itemProfit = ratio[item] * capacity;
                maxProfit += (int) Math.floor(itemProfit);
                capacity = 0;
            }
        }

        return maxProfit;
    }
}
