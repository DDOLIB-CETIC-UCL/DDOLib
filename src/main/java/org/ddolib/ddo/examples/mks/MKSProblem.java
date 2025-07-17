package org.ddolib.ddo.examples.mks;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

public class MKSProblem implements Problem<MKSState> {
    final double[] capa;
    final int[] profit;
    final int[][] weights;
    public final double optimal;

    public MKSProblem(final double[] capa, final int[] profit, final int[][] weight, final double optimal) {
        this.capa = capa;
        this.profit = profit;
        this.weights = weight;
        this.optimal = optimal;
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
        return profit[decision.var()] * decision.val();
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

}
