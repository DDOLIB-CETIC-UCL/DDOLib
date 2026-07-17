package org.ddolib.examples.nolayer.knapsack;

import org.ddolib.nolayer.modeling.Problem;
import org.ddolib.common.util.InvalidSolutionException;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Knapsack Problem (KS), using the no-layer modeling API.
 * <p>
 * Models the classic 0/1 knapsack problem: given a capacity and a list of items each with a
 * weight and a profit, decide which items to take so as to maximize total profit without
 * exceeding the capacity. States are represented by {@link KSState} (current item index and
 * remaining capacity); since the library minimizes, transition costs are negated profits.
 * </p>
 */
public class KSProblem implements Problem<KSState> {

    /** Profit of each item, indexed by item number. */
    public final int[] profit;
    /** Weight of each item, indexed by item number. */
    public final int[] weight;
    /** Total capacity of the knapsack. */
    public final int capa;
    private final int nbItems;
    private final Optional<String> name;

    /**
     * Creates a new named knapsack instance.
     *
     * @param profit profit of each item, indexed by item number
     * @param weight weight of each item, indexed by item number
     * @param capa   total capacity of the knapsack
     * @param name   name of the instance, used in {@link #toString()}
     */
    public KSProblem(int[] profit, int[] weight, int capa, String name) {
        this.profit = profit;
        this.weight = weight;
        this.capa = capa;
        this.nbItems = profit.length;
        this.name = Optional.of(name);
    }

    /**
     * Creates a new unnamed knapsack instance.
     *
     * @param profit profit of each item, indexed by item number
     * @param weight weight of each item, indexed by item number
     * @param capa   total capacity of the knapsack
     */
    public KSProblem(int[] profit, int[] weight, int capa) {
        this.profit = profit;
        this.weight = weight;
        this.capa = capa;
        this.nbItems = profit.length;
        this.name = Optional.empty();
    }

    /**
     * Reads a knapsack instance from a file and returns it with its items sorted by
     * decreasing profit-to-weight ratio.
     *
     * @param fname path to the instance file
     * @return the knapsack instance described by the file
     * @throws java.io.IOException if the instance file cannot be read
     */
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

        return new KSProblem(sortedProfit, sortedWeight, c, fname);
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
        return name.orElse("KSProblem(nbVars:" + nbItems + ")");
    }
}
