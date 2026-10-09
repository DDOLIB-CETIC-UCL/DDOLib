package org.ddolib.examples.layered.boundedknapsack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import org.ddolib.layered.modeling.FastLowerBound;

/**
 * A fast lower bound implementation for the {@link BKSProblem} (Bounded Knapsack Problem).
 *
 * <p>This class computes a lower bound on the optimal solution value from a given state and a
 * subset of variables (items) using a fractional knapsack relaxation. The algorithm sorts the
 * remaining items by their value-to-weight ratio and greedily fills the remaining capacity to
 * approximate the best possible completion from the current partial solution.
 *
 * <p>The lower bound is returned as a negative value (following the solver convention where the
 * objective is to minimize the total cost or maximize the profit).
 *
 * <p><b>Example:</b>
 *
 * <pre>{@code
 * BKSProblem problem = new BKSProblem(values, weights, quantities, capacity);
 * FastLowerBound<Integer> flb = new BKSFastLowerBound(problem);
 * double bound = flb.fastLowerBound(currentCapacity, remainingItems);
 * }</pre>
 *
 * @see BKSProblem
 * @see FastLowerBound
 */
public class BKSFastLowerBound implements FastLowerBound<Integer> {
    /** The bounded knapsack problem instance for which this lower bound is computed. */
    private final BKSProblem problem;

    /**
     * Constructs a fast lower bound evaluator for the given bounded knapsack problem.
     *
     * @param problem the knapsack problem instance containing item values, weights, and capacities
     */
    public BKSFastLowerBound(BKSProblem problem) {
        this.problem = problem;
    }

    /**
     * Computes a fast lower bound for the given state and remaining variables.
     *
     * <p>The algorithm:
     *
     * <ol>
     *   <li>Computes the value-to-weight ratio for each remaining item.
     *   <li>Sorts the items in descending order of their ratio (most efficient items first).
     *   <li>Greedily fills the remaining capacity using as many units as possible of each item,
     *       possibly using a fractional last item (relaxation).
     *   <li>Returns the negative of the total achievable value as the lower bound estimate.
     * </ol>
     *
     * @param state the current capacity remaining in the knapsack
     * @param variables the set of indices of remaining items to consider
     * @return a fast lower bound estimate (as a negative value) of the optimal solution
     */
    @Override
    public double fastLowerBound(Integer state, Set<Integer> variables) {
        Integer[] sorted = variables.toArray(new Integer[0]);
        // most efficient items first
        Arrays.sort(
                sorted,
                Comparator.comparingDouble(
                                (Integer i) -> (double) problem.values[i] / problem.weights[i])
                        .reversed());

        double remainingCapacity = state;
        double maxProfit = 0;
        for (int item : sorted) {
            if (remainingCapacity <= 0) {
                break;
            }
            // as many units as possible, the last item may be taken fractionally
            double quantity =
                    Math.min(problem.quantities[item], remainingCapacity / problem.weights[item]);
            maxProfit += quantity * problem.values[item];
            remainingCapacity -= quantity * problem.weights[item];
        }
        return -maxProfit;
    }
}
