package org.ddolib.nolayer.examples.knapsack;

import org.ddolib.nolayer.modeling.Problem;
import org.ddolib.util.InvalidSolutionException;

import java.util.Iterator;
import java.util.List;

public class KSProblem implements Problem<KSState> {

    public final int[] profit;
    public final int[] weight;
    public final int capa;
    private final int nbItems;

    public KSProblem(int[] profit, int[] weight, int capa) {
        this.profit = profit;
        this.weight = weight;
        this.capa = capa;
        this.nbItems = profit.length;
    }

    public static KSProblem fromFile(final String fname) throws java.io.IOException {
        boolean isFirst = true;
        int count = 0;
        int n = 0;
        int c = 0;
        int[] profit = new int[0];
        int[] weight = new int[0];
        try (final java.io.BufferedReader bf = new java.io.BufferedReader(new java.io.FileReader(fname))) {
            String line;
            while ((line = bf.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                    String[] tokens = line.split("\\s");
                    n = Integer.parseInt(tokens[0]);
                    c = Integer.parseInt(tokens[1]);
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

        Integer[] items = new Integer[n];
        for (int i = 0; i < n; i++) {
            items[i] = i;
        }
        final int[] w = weight;
        final int[] p = profit;

        java.util.Arrays.sort(items, (o1, o2) -> {
            double ratio1 = (double) p[o1] / w[o1];
            double ratio2 = (double) p[o2] / w[o2];
            return Double.compare(ratio2, ratio1);
        });

        int[] sortedProfit = new int[n];
        int[] sortedWeight = new int[n];

        for (int i = 0; i < n; i++) {
            int j = items[i];
            sortedProfit[i] = profit[j];
            sortedWeight[i] = weight[j];
        }

        return new KSProblem(sortedProfit, sortedWeight, c);
    }

    @Override
    public KSState initialState() {
        return new KSState(0, capa);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public boolean isTarget(KSState state) {
        return state.currentItem() >= nbItems;
    }

    @Override
    public Iterator<Integer> domain(KSState state) {
        if (state.remainingCapacity() >= weight[state.currentItem()]) {
            return List.of(0, 1).iterator();
        } else {
            return List.of(0).iterator();
        }
    }

    @Override
    public KSState transition(KSState state, int label) {
        return new KSState(
                state.currentItem() + 1,
                state.remainingCapacity() - label * weight[state.currentItem()]
        );
    }

    @Override
    public double transitionCost(KSState state, int label) {
        return -label * profit[state.currentItem()];
    }

    @Override
    public double evaluate(List<Integer> solution) throws InvalidSolutionException {
        if (solution.size() != nbItems) {
            throw new InvalidSolutionException("Expected " + nbItems + " values, got " + solution.size());
        }
        int totalWeight = 0;
        int totalProfit = 0;
        for (int i = 0; i < nbItems; i++) {
            if (solution.get(i) == 1) {
                totalWeight += weight[i];
                totalProfit += profit[i];
            } else if (solution.get(i) != 0) {
                throw new InvalidSolutionException("Value must be 0 or 1, got " + solution.get(i) + " at index " + i);
            }
        }
        if (totalWeight > capa) {
            throw new InvalidSolutionException("Capacity exceeded: " + totalWeight + " > " + capa);
        }
        return -totalProfit;
    }

    @Override
    public String toString() {
        return "KSProblem(nbVars:" + nbItems + ")";
    }
}
