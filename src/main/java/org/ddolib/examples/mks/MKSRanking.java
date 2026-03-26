package org.ddolib.examples.mks;

import org.ddolib.modeling.StateRanking;
/**
 * Ranking strategy for multi-dimensional Knapsack (MKS) states.
 *
 * <p>
 * This class implements {@link StateRanking} and provides a method to compare
 * two MKS states based on the average remaining capacity across all knapsack dimensions.
 * States with smaller average remaining capacity are considered "smaller" in the ranking.
 */
public class MKSRanking implements StateRanking<MKSState> {
    /**
     * Compares two MKS states based on their average remaining capacities.
     *
     * <p>
     * The average capacity of each state is computed by summing all dimension capacities
     * and dividing by the number of dimensions. Comparison is then performed using
     * {@link Double#compare(double, double)}.
     *
     * @param o1 the first MKS state
     * @param o2 the second MKS state
     * @return a negative integer, zero, or a positive integer as the first state
     *         has less than, equal to, or greater average capacity than the second state
     */
    @Override
    public int compare(MKSState o1, MKSState o2) {
        double avgCapa1 = 0;
        double avgCapa2 = 0;
        for (int dim = 0; dim < o1.capacities.length; dim++) {
            avgCapa1 += o1.capacities[dim];
            avgCapa2 += o2.capacities[dim];
        }
        avgCapa1 /= o1.capacities.length;
        avgCapa2 /= o2.capacities.length;

        return Double.compare(avgCapa1, avgCapa2);
    }
}
