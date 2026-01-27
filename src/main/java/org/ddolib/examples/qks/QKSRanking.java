package org.ddolib.examples.qks;

import org.ddolib.modeling.StateRanking;

/**
 * Ranking strategy for Quadratic Knapsack (SKS) states.
 *
 * <p>
 * This class implements {@link StateRanking} and provides a method to compare
 * two QKS states based on the remaining capacity.
 * States with smaller remaining capacity are considered "smaller" in the ranking.
 */
public class QKSRanking implements StateRanking<QKSState> {
    @Override
    public int compare(QKSState o1, QKSState o2) {
        return Double.compare(o1.capacity, o2.capacity);
    }
}
