package org.ddolib.examples.knapsack;

import org.ddolib.modeling.FastLowerBound;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

/**
 * Fast lower bound heuristic for the Knapsack Problem (KS).
 * <p>
 * This class implements a quick estimation of the lower bound of the optimal solution
 * for a given knapsack state. The heuristic uses a greedy strategy based on the
 * profit-to-weight ratio of items, selecting the most profitable items first
 * until the remaining capacity is exhausted.
 * </p>
 * <p>
 * The returned value is negated to be compatible with solvers that minimize the objective function.
 * </p>
 */
public class KSFastLowerBound implements FastLowerBound<Integer> {
    /**
     * The associated Knapsack problem instance.
     */
    private final KSProblem problem;

    /**
     * Constructs a new fast lower bound heuristic for the given Knapsack problem.
     *
     * @param problem the Knapsack problem instance
     */
    public KSFastLowerBound(KSProblem problem) {
        this.problem = problem;
    }

    /**
     * Computes a fast lower bound for the given knapsack state.
     *
     * @param state     the current remaining capacity of the knapsack
     * @param variables the set of available item indices
     * @return a fast estimate of the lower bound (negated)
     */
    @Override
    public double fastLowerBound(Integer state, Set<Integer> variables) {
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
        return -maxProfit;
    }
}
