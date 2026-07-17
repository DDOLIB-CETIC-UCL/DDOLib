package org.ddolib.examples.nolayer.knapsack;

import org.ddolib.nolayer.modeling.FastLowerBound;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Fast lower bound for the Knapsack Problem (KS), using the no-layer modeling API.
 * <p>
 * The bound is computed by a fractional-relaxation greedy heuristic: remaining items are
 * sorted by decreasing profit-to-weight ratio and packed into the remaining capacity,
 * allowing the last item to be split fractionally. The negated value of this greedy
 * profit is returned, consistently with the library's minimization convention.
 * </p>
 */
public class KSFlb implements FastLowerBound<KSState> {

    private final KSProblem problem;

    /**
     * Creates a new fast lower bound for the given knapsack instance.
     *
     * @param problem the knapsack instance to bound
     */
    public KSFlb(KSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(KSState state) {
        int start = state.currentItem();
        int totalItems = problem.profit.length;
        int n = totalItems - start;

        Integer[] items = new Integer[n];
        for (int i = 0; i < n; i++) items[i] = start + i;

        Arrays.sort(items, Comparator.comparingDouble(
                (Integer i) -> (double) problem.profit[i] / problem.weight[i]).reversed());

        double maxProfit = 0;
        int capacity = state.remainingCapacity();

        for (int item : items) {
            if (capacity <= 0) break;
            int w = problem.weight[item];
            if (capacity >= w) {
                maxProfit += problem.profit[item];
                capacity -= w;
            } else {
                maxProfit += (double) problem.profit[item] / w * capacity;
                capacity = 0;
            }
        }

        return -maxProfit;
    }
}