package org.ddolib.examples.mks;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.Iterator;

import static java.lang.Math.max;
/**
 * Relaxation strategy for the multi-dimensional Knapsack problem (MKS) states.
 *
 * <p>
 * This class implements the {@link Relaxation} interface and provides a way to
 * merge multiple MKS states into a single relaxed state for use in decision diagrams.
 * The relaxation ensures that the merged state overestimates the remaining capacities,
 * which is safe for optimization purposes.
 */
public class MKSRelax implements Relaxation<MKSState> {
    /**
     * Merges multiple MKS states into a single relaxed state.
     *
     * <p>
     * The merged state takes the maximum capacity along each dimension across all
     * input states. This produces a relaxed state that safely overestimates the
     * remaining capacities.
     *
     * @param states an iterator over the states to merge
     * @return a new {@link MKSState} representing the relaxed merged state
     */
    @Override
    public MKSState mergeStates(Iterator<MKSState> states) {
        assert states.hasNext();
        MKSState state = states.next();
        double[] capa = state.capacities.clone();
        while (states.hasNext()) {
            state = states.next();
            for (int dim = 0; dim < capa.length; dim++) {
                capa[dim] = max(capa[dim], state.capacities[dim]);
            }
        }
        return new MKSState(capa);
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
    public double relaxEdge(MKSState from, MKSState to, MKSState merged, Decision d, double cost) {
        return cost;
    }
}
