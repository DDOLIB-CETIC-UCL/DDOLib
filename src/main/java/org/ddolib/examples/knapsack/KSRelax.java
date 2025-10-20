package org.ddolib.examples.knapsack;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.Iterator;
/**
 * Relaxation for the Knapsack Problem (KS).
 * <p>
 * This class implements a simple relaxation of the state space for relaxed decision diagrams.
 * The merged state corresponds to the maximum remaining capacity among all states being merged.
 * </p>
 * <p>
 * The edge relaxation does not modify the cost: it simply returns the original cost.
 * </p>
 *
 * @see Relaxation
 */
public class KSRelax implements Relaxation<Integer> {
    /**
     * Merges multiple states into a single relaxed state.
     * <p>
     * For the Knapsack problem, the merged state is defined as the maximum remaining capacity
     * among the states being merged.
     * </p>
     *
     * @param states an iterator over the states to merge
     * @return the merged state representing the maximum remaining capacity
     */
    @Override
    public Integer mergeStates(final Iterator<Integer> states) {
        int capa = 0;
        while (states.hasNext()) {
            final Integer state = states.next();
            capa = Math.max(capa, state);
        }
        return capa;
    }
    /**
     * Relaxes the cost of an edge between states in the decision diagram.
     * <p>
     * For the Knapsack problem, the cost is not modified by the relaxation.
     * </p>
     *
     * @param from the starting state
     * @param to the ending state
     * @param merged the merged state after relaxation
     * @param d the decision associated with the edge
     * @param cost the original cost of the edge
     * @return the relaxed cost (identical to the original cost in this implementation)
     */
    @Override
    public double relaxEdge(Integer from, Integer to, Integer merged, Decision d, double cost) {
        return cost;
    }

}
