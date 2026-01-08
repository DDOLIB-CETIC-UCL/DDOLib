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

public class MKSProblem implements Problem<MKSState> {
    final double[] capa;
    final int[] profit;
    final int[][] weights;
    public final Optional<Double> optimal;
    final Optional<String> name;
    final double maximalDistance;

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

    @Override
    public Optional<Double> optimalValue() {
        return optimal.map(x -> -x );
    }

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


    @Override
    public int nbVars() {
        return profit.length;
    }

    @Override
    public MKSState initialState() {
        return new MKSState(capa.clone());
    }

    @Override
    public double initialValue() {
        return 0;
    }

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

    @Override
    public MKSState transition(MKSState state, Decision decision) {
        double[] newCapa = state.capacities.clone();
        for (int dim = 0; dim < capa.length; dim++) {
            newCapa[dim] -= weights[decision.var()][dim] * decision.val();
        }
        return new MKSState(newCapa);
    }

    @Override
    public double transitionCost(MKSState state, Decision decision) {
        // If the item is taken (1) the cost is the profit of the item, 0 otherwise
        return -profit[decision.var()] * decision.val();
    }

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
