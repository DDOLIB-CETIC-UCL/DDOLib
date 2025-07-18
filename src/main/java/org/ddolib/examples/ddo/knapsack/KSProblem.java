package org.ddolib.examples.ddo.knapsack;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

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
    final int[] profit;
    final int[] weight;
    private Optional<Double> optimal = Optional.empty();

    private Optional<String> name = Optional.empty();

    public KSProblem(final int capa, final int[] profit, final int[] weight, final double optimal) {
        this.capa = capa;
        this.profit = profit;
        this.weight = weight;
        this.optimal = Optional.of(optimal);
    }

    public KSProblem(final int capa, final int[] profit, final int[] weight) {
        this.capa = capa;
        this.profit = profit;
        this.weight = weight;
    }

    public void setName(String name) {
        this.name = Optional.of(name);
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
        return profit[decision.var()] * decision.val();
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }
}

