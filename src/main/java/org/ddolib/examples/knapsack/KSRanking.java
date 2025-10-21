package org.ddolib.examples.knapsack;

import org.ddolib.modeling.StateRanking;
/**
 * State ranking for the Knapsack Problem (KS).
 * <p>
 * The ranking is based on the remaining capacity of the knapsack.
 * States with a larger remaining capacity are considered "better" (i.e., ranked higher).
 * </p>
 * <p>
 * This ranking can be used by solvers to prioritize states during search.
 * </p>
 */
public class KSRanking implements StateRanking<Integer> {
    /**
     * Compares two states based on their remaining capacity.
     *
     * @param o1 remaining capacity of the first state
     * @param o2 remaining capacity of the second state
     * @return a negative integer, zero, or a positive integer if the first state
     *         has less, equal, or greater remaining capacity than the second state
     */
    @Override
    public int compare(final Integer o1, final Integer o2) {
        return o1 - o2;
    }
}
