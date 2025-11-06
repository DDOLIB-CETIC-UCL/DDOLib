package org.ddolib.examples.boundedknapsack;


import org.ddolib.modeling.Dominance;
/**
 * Implementation of a dominance rule for the Bounded Knapsack (BKS) problem.
 * <p>
 * This class defines how two states (represented here by integer capacities)
 * can dominate one another in the search space. In this context, a state
 * with a smaller capacity is considered dominated by a state with a larger one,
 * since it provides fewer remaining resources for future item selections.
 * </p>
 *
 * <p>
 * The dominance mechanism helps prune the search space efficiently in
 * dynamic programming or branch-and-bound algorithms.
 * </p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * BKSDominance dominance = new BKSDominance();
 * boolean result = dominance.isDominatedOrEqual(5, 10); // true, since 5 < 10
 * }</pre>
 *
 * @see Dominance
 */
public class BKSDominance implements Dominance<Integer> {
    /**
     * Returns the dominance key for a given state.
     * <p>
     * In this simplified implementation, all states share the same key (0),
     * meaning that dominance comparisons are applied globally without grouping.
     * </p>
     *
     * @param capa The state or capacity value for which the key is requested.
     * @return Always returns {@code 0}, indicating no partitioning by key.
     */
    @Override
    public Integer getKey(Integer capa) {
        return 0;
    }
    /**
     * Checks whether one state (capacity) is dominated or equal to another.
     * <p>
     * In this implementation, {@code capa1} is considered dominated or equal
     * to {@code capa2} if it is strictly smaller.
     * </p>
     *
     * @param capa1 The first capacity (potentially dominated).
     * @param capa2 The second capacity (potential dominator).
     * @return {@code true} if {@code capa1 < capa2}, {@code false} otherwise.
     */
    @Override
    public boolean isDominatedOrEqual(Integer capa1, Integer capa2) {
        return capa1 < capa2;
    }
}
