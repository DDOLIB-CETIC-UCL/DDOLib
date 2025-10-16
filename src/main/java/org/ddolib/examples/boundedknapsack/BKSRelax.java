package org.ddolib.examples.boundedknapsack;


import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.Iterator;
/**
 * A relaxation strategy for the Bounded Knapsack Problem (BKP) used in
 * relaxed decision diagrams.
 * <p>
 * This class implements the {@link Relaxation} interface, providing a way
 * to merge multiple states and optionally relax edge costs in the construction
 * of a relaxed decision diagram (DD).
 * </p>
 * <p>
 * <b>Merge strategy:</b> the merged state is the maximum remaining capacity
 * among the given states. This ensures that the relaxation over-approximates
 * the remaining capacity and maintains feasibility in the relaxed DD.
 * </p>
 * <p>
 * <b>Edge relaxation:</b> the cost of transitions is not modified and returned
 * unchanged. This simple strategy preserves the original objective value.
 * </p>
 *
 * @see Relaxation
 * @see BKSProblem
 */
public class BKSRelax implements Relaxation<Integer> {
    /**
     * Default constructor.
     */
    public BKSRelax() {
    }
    /**
     * Merges a set of states into a single representative state for a relaxed DD.
     * <p>
     * For BKP, this implementation returns the maximum remaining capacity
     * among the given states.
     * </p>
     *
     * @param states an iterator over the states to merge
     * @return the merged state
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
     * Optionally relaxes the edge cost when transitioning from a state to a merged state.
     * <p>
     * In this implementation, the edge cost is not modified and returned as-is.
     * </p>
     *
     * @param from   the origin state
     * @param to     the destination state
     * @param merged the merged state after relaxation
     * @param d      the decision taken to move from {@code from} to {@code to}
     * @param cost   the original cost of the edge
     * @return the relaxed cost (unchanged in this implementation)
     */
    @Override
    public double relaxEdge(Integer from, Integer to, Integer merged, Decision d, double cost) {
        return cost;
    }

}

