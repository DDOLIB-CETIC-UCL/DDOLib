package org.ddolib.examples.knapsack;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Represents an instance of the Knapsack Problem (KS).
 * <p>
 * The state of the problem is the remaining capacity of the knapsack (represented as an {@link Integer}).
 * Decisions correspond to selecting or not selecting a specific item (1 for selected, 0 for not selected).
 * </p>
 * <p>
 * This class supports creating instances from:
 * </p>
 * <ul>
 *     <li>Explicit arrays of profits and weights, with or without a known optimal value.</li>
 *     <li>A file formatted with the number of items, capacity, (optional) optimal value,
 *     and a list of item profits and weights.</li>
 * </ul>
 *
 * <p>
 * Costs are negated profits to allow using solvers designed for minimization.
 * </p>
 */
public class KSProblem implements Problem<Integer> {

    /**
     * Maximum capacity of the knapsack.
     */
    public final int capa;

    /**
     * Profits of the items.
     */
    public final int[] profit;

    /**
     * Weights of the items.
     */
    public final int[] weight;

    /**
     * Optional known optimal solution value.
     */
    public final Optional<Double> optimal;

    /**
     * Optional name of the instance (usually the filename).
     */
    public final Optional<String> name;

    /**
     * Constructs a Knapsack problem with given capacity, profits, weights, and known optimal value.
     *
     * @param capa    maximum capacity of the knapsack
     * @param profit  array of item profits
     * @param weight  array of item weights
     * @param optimal known optimal value
     */
    public KSProblem(final int capa, final int[] profit, final int[] weight, final double optimal) {
        this.capa = capa;
        this.profit = profit;
        this.weight = weight;
        this.optimal = Optional.of(optimal);
        this.name = Optional.empty();
    }

    /**
     * Constructs a Knapsack problem with given capacity, profits, and weights.
     * No optimal value is provided.
     *
     * @param capa   maximum capacity of the knapsack
     * @param profit array of item profits
     * @param weight array of item weights
     */
    public KSProblem(final int capa, final int[] profit, final int[] weight) {
        this.capa = capa;
        this.profit = profit;
        this.weight = weight;
        this.optimal = Optional.empty();
        this.name = Optional.empty();
    }

    /**
     * Constructs a Knapsack problem from a file.
     * <p>
     * The file format should contain:
     * <ul>
     *     <li>First line: number of items, capacity, (optional) optimal value.</li>
     *     <li>Following lines: item profit and weight for each item.</li>
     * </ul>
     *
     * @param fname path to the file
     * @throws IOException if an I/O error occurs while reading the file
     */
    public KSProblem(final String fname) throws IOException {
        boolean isFirst = true;
        int count = 0;
        int n = 0;
        final File f = new File(fname);
        String line;
        int c = 0;
        int[] profit = new int[0];
        int[] weight = new int[0];
        Optional<Double> optimal = Optional.empty();
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
            while ((line = bf.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                    String[] tokens = line.split("\\s");
                    n = Integer.parseInt(tokens[0]);
                    c = Integer.parseInt(tokens[1]);
                    if (tokens.length == 3) {
                        optimal = Optional.of(Double.parseDouble(tokens[2]));
                    }
                    profit = new int[n];
                    weight = new int[n];
                } else {
                    if (count < n) {
                        String[] tokens = line.split("\\s");
                        profit[count] = Integer.parseInt(tokens[0]);
                        weight[count] = Integer.parseInt(tokens[1]);
                        count++;
                    }
                }
            }
        }
        this.capa = c;
        this.profit = profit;
        this.weight = weight;
        this.optimal = optimal;
        this.name = Optional.of(fname);
    }

    @Override
    public String toString() {
        return name.orElse(String.format("Max capacity: %d\nProfits: %s\nWeights: %s",
                capa,
                Arrays.toString(profit),
                Arrays.toString(weight)));
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
    public double initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(Integer state, int var) {
        if (state >= weight[var]) { // The item can be taken or not
            return Arrays.asList(1, 0).iterator();
        } else { // The item cannot be taken
            return List.of(0).iterator();
        }
    }

    @Override
    public Integer transition(Integer state, Decision decision) {
        // If the item is taken (1), we decrease the capacity of the knapsack, otherwise leave it unchanged
        return state - weight[decision.var()] * decision.val();
    }

    @Override
    public double transitionCost(Integer state, Decision decision) {
        // If the item is taken (1) the cost is the profit of the item, 0 otherwise
        return -profit[decision.var()] * decision.val();
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal.map(x -> -x);
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not cover all " +
                    "the %d variables", Arrays.toString(solution), nbVars()));
        }

        int totalProfit = 0;
        int totalWeight = 0;
        for (int i = 0; i < solution.length; i++) {
            totalProfit += profit[i] * solution[i];
            totalWeight += weight[i] * solution[i];
        }

        if (totalWeight > capa) {
            String msg = String.format("The weight of %s (%d) exceeds the capatity of the " +
                    "knapsack (%d)", Arrays.toString(solution), totalWeight, capa);
            throw new InvalidSolutionException(msg);
        }

        return -totalProfit;
    }
}

