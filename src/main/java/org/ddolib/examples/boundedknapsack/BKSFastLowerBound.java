package org.ddolib.examples.boundedknapsack;

import org.ddolib.modeling.FastLowerBound;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

/**
 * A fast lower bound implementation for the {@link BKSProblem} (Bounded Knapsack Problem).
 * <p>
 * This class computes a lower bound on the optimal solution value from a given
 * state and a subset of variables (items) using a fractional knapsack relaxation.
 * The algorithm sorts the remaining items by their value-to-weight ratio and
 * greedily fills the remaining capacity to approximate the best possible completion
 * from the current partial solution.
 * </p>
 *
 * <p>
 * The lower bound is returned as a negative value (following the solver convention
 * where the objective is to minimize the total cost or maximize the profit).
 * </p>
 *
 * <p><b>Example:</b></p>
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
    /**
     * The bounded knapsack problem instance for which this lower bound is computed.
     */
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
     * <p>
     * The algorithm:
     * <ol>
     *   <li>Computes the value-to-weight ratio for each remaining item.</li>
     *   <li>Sorts the items in descending order of their ratio (most efficient items first).</li>
     *   <li>Greedily fills the remaining capacity using as many units as possible of each item,
     *       possibly using a fractional last item (relaxation).</li>
     *   <li>Returns the negative of the total achievable value as the lower bound estimate.</li>
     * </ol>
     *
     * @param state     the current capacity remaining in the knapsack
     * @param variables the set of indices of remaining items to consider
     * @return a fast lower bound estimate (as a negative value) of the optimal solution
     */
    @Override
    public double fastLowerBound(Integer state, Set<Integer> variables) {
        double[] ratio = new double[problem.nbVars()];
        int capacity = state;
        for (int v : variables) {
            ratio[v] = ((double) problem.values[v] / problem.weights[v]);
        }

        class RatioComparator implements Comparator<Integer> {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Double.compare(ratio[o1], ratio[o2]);
            }
        }

        Integer[] sorted = variables.toArray(new Integer[0]);
        Arrays.sort(sorted, new RatioComparator().reversed());

        int currentSolutionValue = 0;
        int currentTotalWeight = 0;
        Iterator<Integer> itemIterator = Arrays.stream(sorted).iterator();
        while (itemIterator.hasNext()) {
            int item = itemIterator.next();
            if (currentTotalWeight + problem.weights[item] < capacity) {
                int x = Math.min(problem.quantities[item], (capacity - currentTotalWeight) / problem.weights[item]);
                currentSolutionValue += x * problem.weights[item];
                currentSolutionValue += x * problem.values[item];
            }
        }
        return -currentSolutionValue;
    }
}
