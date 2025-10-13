package org.ddolib.examples.knapsack;

import org.ddolib.ddo.core.Decision;
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
 * Class to model the Knapsack problem.
 * The state is the remaining capacity of the knapsack (an Integer thus).
 * The decision is the item to take or not (1 or 0).
 */
public class KSProblem implements Problem<Integer> {

    public final int capa;
    public final int[] profit;
    public final int[] weight;
    public final Optional<Double> optimal;
    public final Optional<String> name;

    public KSProblem(final int capa, final int[] profit, final int[] weight, final double optimal) {
        this.capa = capa;
        this.profit = profit;
        this.weight = weight;
        this.optimal = Optional.of(optimal);
        this.name = Optional.empty();
    }

    public KSProblem(final int capa, final int[] profit, final int[] weight) {
        this.capa = capa;
        this.profit = profit;
        this.weight = weight;
        this.optimal = Optional.empty();
        this.name = Optional.empty();
    }


    public KSProblem(final String fname) throws IOException {
        boolean isFirst = true;
        int count = 0;
        int n = 0;
        final File f = new File(fname);
        String line;
        int c = 0;
        int [] profit = new int[0];
        int [] weight = new int[0];
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

    private static class PinReadContext {
        boolean isFirst = true;
        int n = 0;
        int count = 0;
        int capa = 0;
        int[] profit = new int[0];
        int[] weight = new int[0];
        Integer optimal = null;
    }

    @Override
    public String toString() {
        if (name.isPresent()) {
            return name.get();
        } else {
            return String.format("Max capacity: %d\nProfits: %s\nWeights: %s",
                    capa,
                    Arrays.toString(profit),
                    Arrays.toString(weight));
        }
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
}

