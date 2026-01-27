package org.ddolib.examples.qks;

import org.ddolib.modeling.Dominance;

public class QKSDominance implements Dominance<QKSState> {
    /**
     * Returns a key for grouping states in dominance checks.
     *
     * <p>
     * Here, all states share the same key (0), meaning that all states are comparable
     * for dominance against each other.
     *
     * @param state the state
     * @return the key for dominance grouping (always 0)
     */
    @Override
    public Object getKey(QKSState state) {
        return 0;
    }

    /**
     * Determines whether {@code state1} is dominated by or equal to {@code state2}.
     *
     * <p>
     * {@code state1} is dominated if its remaining capacity and all its every potential profits are
     * less or equal than the corresponding value in {@code state2}.
     *
     * @param state1 the first state to compare
     * @param state2 the second state to compare
     * @return {@code true} if {@code state1} is dominated by or equal to {@code state2}, {@code false} otherwise
     */
    @Override
    public boolean isDominatedOrEqual(QKSState state1, QKSState state2) {
        if (state1.capacity > state2.capacity) {
            return false;
        }
        for (int i = 0; i < state1.itemsProfit.length; i++) {
            if (state1.itemsProfit[i] > state2.itemsProfit[i]) {
                return false;
            }
        }
        return true;
    }
}
