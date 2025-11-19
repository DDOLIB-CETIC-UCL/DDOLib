package org.ddolib.examples.boundedknapsack;

import org.ddolib.modeling.StateRanking;
/**
 * A default state ranking implementation for the Bounded Knapsack Problem (BKP).
 * <p>
 * This class implements the {@link StateRanking} interface for states represented
 * as remaining capacities (integers). It provides a simple comparison function
 * that ranks states in ascending order of remaining capacity.
 * </p>
 * <p>
 * This ranking can be used by solvers to prioritize exploration of states with
 * smaller remaining capacity first (or larger if reversed), which may be useful
 * for decision diagram pruning or heuristic-guided search.
 * </p>
 *
 * @see StateRanking
 * @see BKSProblem
 */
public class BKSRanking implements StateRanking<Integer> {
    /**
     * Compares two states based on their remaining capacity.
     *
     * @param o1 the first state
     * @param o2 the second state
     * @return a negative integer if o1 has smaller capacity than o2,
     *         zero if they are equal,
     *         a positive integer if o1 has larger capacity than o2
     */
    @Override
    public int compare(final Integer o1, final Integer o2) {
        return o1 - o2;
    }
}
