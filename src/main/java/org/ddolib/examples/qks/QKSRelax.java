package org.ddolib.examples.qks;

import org.ddolib.ddo.core.Decision;
import org.ddolib.examples.mks.MKSState;
import org.ddolib.modeling.Relaxation;

import java.util.Iterator;

/**
 * Relaxation strategy for the Quadratic Knapsack problem (QKS) states.
 *
 * <p>
 * This class implements the {@link Relaxation} interface and provides a way to
 * merge multiple QKS states into a single relaxed state for use in decision diagrams.
 * The relaxation ensures that the merged state overestimates the remaining capacities,
 * which is safe for optimization purposes.
 */
public class QKSRelax implements Relaxation<QKSState> {

    /**
     * Merges multiple QKS states into a single relaxed state.
     *
     * <p>
     *
     * @param states an iterator over the states to merge
     * @return a new {@link QKSState} representing the relaxed merged state
     */
    @Override
    public QKSState mergeStates(Iterator<QKSState> states) {
        assert states.hasNext();
        QKSState state = states.next();
        double capacity = state.capacity;
        double[] itemsProfit = state.itemsProfit.clone();
        while (states.hasNext()) {
            state = states.next();
            capacity = Math.max(capacity, state.capacity);
            for (int i = 0; i < itemsProfit.length; i++) {
                itemsProfit[i] = Math.max(itemsProfit[i], state.itemsProfit[i]);
            }
        }

        return new QKSState(capacity, itemsProfit, state.remainingItems);
    }

    /**
     * Returns the cost of an edge in the relaxed decision diagram.
     *
     * <p>
     * For this relaxation, the edge cost is unchanged and simply returns the
     * original cost.
     *
     * @param from the source state
     * @param to the destination state
     * @param merged the merged state corresponding to the relaxation
     * @param d the decision taken along this edge
     * @param cost the original cost of the edge
     * @return the relaxed edge cost, equal to {@code cost}
     */
    @Override
    public double relaxEdge(QKSState from, QKSState to, QKSState merged, Decision d, double cost) {
        return cost;
    }
}
