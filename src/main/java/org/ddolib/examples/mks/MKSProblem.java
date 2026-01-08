package org.ddolib.examples.mks;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.Optional;
/**
 * Represents a Multi-dimensional Knapsack Problem (MKS) as a {@link Problem} for decision diagram optimization.
 *
 * <p>
 * Each instance defines a set of items, their profits, and weights along multiple dimensions,
 * along with the capacities of the knapsacks. This class supports evaluation of solutions,
 * state transitions, and generation of variable domains.
 *
 * <p>
 * States in this problem are represented by {@link MKSState}, which tracks the remaining capacities
 * in each dimension.
 *
 * <p>
 * The problem can be constructed either programmatically or loaded from a file.
 * The class also provides an optional known optimal solution for testing purposes.
 */
public class MKSProblem implements Problem<MKSState> {
    /** Capacities of each knapsack dimension. */
    final double[] capa;
    /** Profit of each item. */
    final int[] profit;
    /** Weights of each item along each dimension. */
    final int[][] weights;
    /** Optional known optimal solution value. */
    public final Optional<Double> optimal;
    /** Optional problem name or file name. */
    final Optional<String> name;
    /** Maximal Euclidean distance of the capacities, used for normalization or heuristics. */
    final double maximalDistance;
    /**
     * Constructs an MKSProblem with the given capacities, profits, and weights.
     *
     * @param capa the capacities of each knapsack dimension
     * @param profit the profit of each item
     * @param weight the weight of each item along each dimension
     * @param optimal the known optimal solution value
     */
    public MKSProblem(final double[] capa, final int[] profit, final int[][] weight, final double optimal) {
        this.capa = capa;
        this.profit = profit;
        this.weights = weight;
        this.optimal = Optional.of(optimal);
        this.name = Optional.empty();

        double distance = 0.0;
        for (int i = 0; i < capa.length; i++) {
            distance += Math.pow(capa[i], 2);
        }
        maximalDistance = Math.sqrt(distance);
    }
    /**
     * Loads an MKSProblem from a text file.
     *
     * <p>
     * The file format is expected to contain:
     * <ul>
     *   <li>First line: number of items, number of dimensions, optional optimal value</li>
     *   <li>Second line: capacities of each dimension</li>
     *   <li>Next lines: profit and weights of each item (profit first, then weights)</li>
     * </ul>
     *
     * @param fname the path to the file
     * @throws IOException if the file cannot be read
     */
    public MKSProblem(final String fname) throws IOException {
        final File f = new File(fname);
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
            final PinReadContext context = new PinReadContext();
            bf.lines().forEachOrdered((String s) -> {
                if (context.isFirst) {
                    context.isFirst = false;
                    context.isSecond = true;
                    String[] tokens = s.split("\\s");
                    context.n = Integer.parseInt(tokens[0]);
                    context.dimensions = Integer.parseInt(tokens[1]);

                    if (tokens.length == 3) {
                        context.optimal = Optional.of(Double.parseDouble(tokens[2]));
                    }

                    context.profit = new int[context.n];
                    context.weights = new int[context.n][context.dimensions];
                    context.capa = new double[context.dimensions];
                } else if (context.isSecond) {
                    context.isSecond = false;
                    String[] tokens = s.split("\\s");
                    assert tokens.length == context.dimensions;
                    for (int i = 0; i < context.dimensions; i++) {
                        context.capa[i] = Integer.parseInt(tokens[i]);
                    }
                } else {
                    if (context.count < context.n) {
                        String[] tokens = s.split("\\s");
                        assert tokens.length == context.dimensions+1;
                        context.profit[context.count] = Integer.parseInt(tokens[0]);
                        for (int i = 0; i < context.dimensions; i++) {
                            context.weights[context.count][i] = Integer.parseInt(tokens[i+1]);
                        }
                        context.count++;
                    }
                }
            });
            this.capa = context.capa;
            this.profit = context.profit;
            this.weights = context.weights;
            this.optimal = context.optimal;
            this.name = Optional.of(f.getName());

            double distance = 0.0;
            for (int i = 0; i < capa.length; i++) {
                distance += Math.pow(capa[i], 2);
            }
            maximalDistance = Math.sqrt(distance);
        }
    }
    /**
     * Returns the optional optimal value (negated to follow minimization conventions in DDO).
     *
     * @return optional negated optimal value
     */
    @Override
    public Optional<Double> optimalValue() {
        return optimal.map(x -> -x );
    }
    /**
     * Evaluates the cost of a given solution.
     *
     * <p>
     * Checks that the solution respects knapsack capacities; if violated,
     * an {@link InvalidSolutionException} is thrown.
     *
     * @param solution the selection vector of items (1 = taken, 0 = not taken)
     * @return the negated total profit of the solution
     * @throws InvalidSolutionException if the solution is invalid
     */
    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        if (solution.length != nbVars()) {
            throw new InvalidSolutionException(String.format("The solution %s does not cover all " +
                    "the %d variables", Arrays.toString(solution), nbVars()));
        }

        int totalProfit = 0;
        int[] totalWeights = new int[this.capa.length];
        for (int i = 0; i < solution.length; i++) {
            totalProfit += profit[i] * solution[i];
            for (int j = 0; j < totalWeights.length; j++) {
                totalWeights[j] += weights[i][j] * solution[i];
            }
        }

        for (int dim = 0; dim < this.capa.length; dim++) {
            if (totalWeights[dim] > capa[dim]) {
                String msg = String.format("The weight of %s (%d) exceeds the capacity of the " +
                        "knapsack (%d)", Arrays.toString(solution), totalWeights[dim], capa);
                throw new InvalidSolutionException(msg);
            }
        }


        return -totalProfit;
    }

    /**
     * Returns the number of decision variables (items).
     *
     * @return number of items
     */
    @Override
    public int nbVars() {
        return profit.length;
    }
    /**
     * Returns the initial state for this problem, representing full capacities.
     *
     * @return initial {@link MKSState}
     */
    @Override
    public MKSState initialState() {
        return new MKSState(capa.clone());
    }
    /**
     * Returns the initial value associated with the initial state.
     *
     * @return 0
     */
    @Override
    public double initialValue() {
        return 0;
    }
    /**
     * Returns an iterator over the domain of a variable (item) in a given state.
     *
     * <p>
     * An item can be either taken (1) or not taken (0), depending on remaining capacities.
     *
     * @param state the current MKS state
     * @param var the index of the variable (item)
     * @return iterator over possible decisions (0 or 1)
     */
    @Override
    public Iterator<Integer> domain(MKSState state, int var) {
        for (int dim = 0; dim < capa.length; dim++) {
            // the item cannot be taken
            if (state.capacities[dim] < weights[var][dim]){
                return List.of(0).iterator();
            }
        } // The item can be taken or not
        return List.of(1,0).iterator();
    }
    /**
     * Computes the state resulting from taking a decision in the current state.
     *
     * @param state the current MKS state
     * @param decision the decision to apply
     * @return the resulting {@link MKSState} after the decision
     */
    @Override
    public MKSState transition(MKSState state, Decision decision) {
        double[] newCapa = state.capacities.clone();
        for (int dim = 0; dim < capa.length; dim++) {
            newCapa[dim] -= weights[decision.var()][dim] * decision.val();
        }
        return new MKSState(newCapa);
    }
    /**
     * Computes the cost of taking a decision in a given state.
     *
     * <p>
     * The cost is equal to the negative profit if the item is taken, or 0 otherwise.
     *
     * @param state the current MKS state
     * @param decision the decision applied
     * @return the transition cost
     */
    @Override
    public double transitionCost(MKSState state, Decision decision) {
        // If the item is taken (1) the cost is the profit of the item, 0 otherwise
        return -profit[decision.var()] * decision.val();
    }
    /**
     * Returns a human-readable string representation of the problem,
     * including capacities, items' profits, weights, and known optimal value.
     *
     * @return string representation of the problem
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Capacities: ").append(Arrays.toString(capa)).append("\n");
        builder.append("Optimal: ").append(optimal).append("\n");
        for (int item= 0; item < profit.length; item++) {
            builder.append("Item: ").append(profit[item]).append(", ").append(Arrays.toString(weights[item])).append("\n");
        }
        return builder.toString();
    }


    private static class PinReadContext {
        boolean isFirst = true;
        boolean isSecond = false;
        int n = 0;
        int dimensions = 0;
        int count = 0;
        double[] capa = new double[0];
        int[] profit = new int[0];
        int[][] weights = new int[0][0];
        Optional<Double> optimal = Optional.empty();
    }

}
