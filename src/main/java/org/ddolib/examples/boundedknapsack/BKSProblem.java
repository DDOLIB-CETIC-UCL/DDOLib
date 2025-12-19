package org.ddolib.examples.boundedknapsack;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

/**
 * Represents an instance of the <b>Bounded Knapsack Problem (BKP)</b>.
 *
 * <p>
 * This class implements the {@link Problem} interface and can be used by generic
 * optimization solvers such as Dynamic Decision Diagrams (DD), A*, or Anytime Column Search (ACS).
 * </p>
 *
 * @see BKSFastLowerBound
 * @see Decision
 */
public class BKSProblem implements Problem<Integer> {
    public final int capacity;
    public final int[] values;
    public final int[] weights;
    public final int[] quantities;

    /**
     * Constructs a bounded knapsack problem from explicitly given parameters.
     *
     * @param capacity   the total capacity of the knapsack
     * @param values     an array containing the profit (value) of each item
     * @param weights    an array containing the weight of each item
     * @param quantities an array containing the maximum available quantity of each item
     */
    public BKSProblem(int capacity, int[] values, int[] weights, int[] quantities) {
        this.capacity = capacity;
        this.values = values;
        this.weights = weights;
        this.quantities = quantities;
    }

    /**
     * Randomly generates an instance of the bounded knapsack problem.
     * <p>
     * The generated instance is controlled by a type of correlation between
     * item weights and values, as described in {@link InstanceType}.
     * </p>
     *
     * @param n     the number of items
     * @param range the upper bound for weights and profits
     * @param type  the correlation type between weight and profit
     * @param seed  the random seed used for reproducibility
     */

    public BKSProblem(int n, int range, InstanceType type, long seed) {
        Random rand = new Random(seed);

        int[] values = new int[n];
        int[] weights = new int[n];
        int[] quantity = new int[n];
        int totalWeight = 0;
        for (int j = 0; j < n; j++) {
            int w = 1 + rand.nextInt(range);
            int p;
            switch (type) {
                case UNCORRELATED:
                    p = 1 + rand.nextInt(range);
                    break;
                case WEAKLY_CORRELATED:
                    int minP = Math.max(1, w - range / 10);
                    int maxP = Math.min(range, w + range / 10);
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

        this.capacity = (int) (0.5 * totalWeight); // capacity = 50% of total
        this.values = values;
        this.weights = weights;
        this.quantities = quantity;
    }

    /**
     * Returns the number of variables (items) in the knapsack.
     *
     * @return the number of items
     */
    @Override
    public int nbVars() {
        return values.length;
    }

    /**
     * Returns the initial state of the problem, which corresponds to the
     * remaining capacity of the knapsack before adding any items.
     *
     * @return the initial remaining capacity
     */
    @Override
    public Integer initialState() {
        return capacity;
    }

    /**
     * Returns the initial objective value of the problem.
     *
     * @return the initial value (0.0)
     */
    @Override
    public double initialValue() {
        return 0.0;
    }

    /**
     * Returns the domain (set of possible values) for a given variable (item),
     * given the current remaining capacity.
     * <p>
     * For each item, the decision variable represents the number of copies
     * to include in the knapsack, constrained by both available quantity
     * and remaining capacity.
     * </p>
     *
     * @param state the current remaining capacity
     * @param var   the index of the item
     * @return an iterator over possible quantities for the given item
     */
    @Override
    public Iterator<Integer> domain(Integer state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        domain.add(0);
        for (int v = 1; v <= quantities[var]; v++) {
            if (state >= v * weights[var]) {
                domain.add(v);
            }
        }
        return domain.iterator();
    }

    /**
     * Computes the next state after making a decision on an item.
     *
     * @param state    the current remaining capacity
     * @param decision the decision specifying which item and how many copies to include
     * @return the updated remaining capacity after including the chosen number of items
     */
    @Override
    public Integer transition(Integer state, Decision decision) {
        // If the item is taken (1), we decrease the capacity of the knapsack, otherwise leave it unchanged
        return state - weights[decision.var()] * decision.val();
    }

    /**
     * Computes the transition cost associated with a decision.
     * <p>
     * Since this problem is typically formulated as a maximization,
     * this method returns the <b>negative profit</b> to fit a minimization framework.
     * </p>
     *
     * @param state    the current remaining capacity
     * @param decision the decision specifying which item and how many copies to include
     * @return the negative profit of the chosen items
     */
    @Override
    public double transitionCost(Integer state, Decision decision) {
        // If the item is taken (1) the cost is the profit of the item, 0 otherwise
        return -values[decision.var()] * decision.val();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(String.format("BKS: %d vars\n", nbVars()));
        sb.append("Max Capacity: ").append(capacity).append("\n");
        for (int i = 0; i < nbVars(); i++) {
            sb.append(String.format("%d - ", i));
            sb.append("value: ").append(values[i]).append(" - ");
            sb.append("weight: ").append(weights[i]).append(" - ");
            sb.append("quantity: ").append(quantities[i]).append("\n");
        }

        return sb.toString();
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not match " +
                    "the number %d variables", Arrays.toString(solution), nbVars()));
        }

        int value = 0;
        int weight = 0;
        for (int i = 0; i < nbVars(); i++) {
            int quantity = solution[i];
            if (quantity > quantities[i]) {
                String msg = String.format("The object %d is selected %d times. Its maximum " +
                        "quantity is %d", i, quantity, quantities[i]);
                throw new InvalidSolutionException(msg);
            }
            value += quantity * values[i];
            weight += quantity * weights[i];
        }

        if (weight > capacity) {
            String msg = String.format("The weight of %s (%d) exceeds the capatity of the " +
                    "knapsack (%d)", Arrays.toString(solution), weight, capacity);
            throw new InvalidSolutionException(msg);
        }
        return -value;
    }

    /**
     * Enumeration defining possible correlation types between item
     * weights and profits when generating random instances.
     */
    public enum InstanceType {
        UNCORRELATED, // Profit and weight are independent
        WEAKLY_CORRELATED, //Profits roughly follow weights, but not exactly
        STRONGLY_CORRELATED, // Profit = weight + constant
        SUBSET_SUM // Profits exactly equal weights
    }
}
