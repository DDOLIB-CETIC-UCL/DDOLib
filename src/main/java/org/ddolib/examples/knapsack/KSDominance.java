package org.ddolib.examples.knapsack;

import org.ddolib.modeling.Dominance;
/**
 * Dominance relation for the Knapsack Problem (KS).
 * <p>
 * This dominance checker is used to prune the search space in a decision diagram or
 * other solver by identifying states that are dominated and can therefore be discarded.
 * </p>
 * <p>
 * In this implementation:
 * </p>
 * <ul>
 *     <li>{@link #getKey(Integer)} always returns 0, indicating a single dominance key for all states.</li>
 *     <li>{@link #isDominatedOrEqual(Integer, Integer)} considers a state {@code capa1} dominated
 *     by {@code capa2} if {@code capa1 <= capa2}, i.e., a knapsack state with less or equal remaining capacity
 *     is dominated by one with more remaining capacity.</li>
 * </ul>
 */
public class KSDominance implements Dominance<Integer> {
    /**
     * Returns the key used for grouping states in the dominance checker.
     *
     * @param capa the current state (remaining capacity)
     * @return 0, indicating a single key for all states
     */
    @Override
    public Integer getKey(Integer capa) {
        return 0;
    }
    /**
     * Checks whether one state is dominated by another.
     *
     * @param capa1 the first state (remaining capacity)
     * @param capa2 the second state (remaining capacity)
     * @return {@code true} if {@code capa1 <= capa2}, otherwise {@code false}
     */
    @Override
    public boolean isDominatedOrEqual(Integer capa1, Integer capa2) {
        return capa1 <= capa2;
    }
}