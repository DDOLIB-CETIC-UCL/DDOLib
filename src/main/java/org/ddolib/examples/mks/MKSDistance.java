package org.ddolib.examples.mks;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import static org.ddolib.util.DistanceUtil.euclideanDistance;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
/**
 * Computes a normalized distance between Multi-dimensional Knapsack (MKS) states.
 *
 * <p>
 * This class implements {@link StateDistance} for {@link MKSState} objects. The distance
 * is based on the Euclidean distance between the remaining capacities of two states,
 * normalized by the maximal distance of the knapsack capacities.
 *
 * <p>
 * This distance can be used in clustering or merging strategies in decision diagram
 * optimization algorithms to guide state aggregation.
 */
public class MKSDistance implements StateDistance<MKSState> {
    /** The MKS problem instance for which distances are computed. */
    final MKSProblem instance;
    /**
     * Constructs a distance evaluator for the given MKS problem.
     *
     * @param instance the multi-dimensional knapsack problem instance
     */
    public MKSDistance(MKSProblem instance) {
        this.instance = instance;
    }
    /**
     * Computes the normalized Euclidean distance between two MKS states.
     *
     * @param a the first state
     * @param b the second state
     * @return the normalized distance between {@code a} and {@code b}
     */
    @Override
    public double distance(MKSState a, MKSState b) {
        return euclideanDistance(a.capacities, b.capacities) / instance.maximalDistance;
    }
    /**
     * Computes the normalized Euclidean distance from the given state to the initial state.
     *
     * @param a the state
     * @return the normalized distance from {@code a} to the initial state
     */
    @Override
    public double distanceWithRoot(MKSState a) {
        return euclideanDistance(a.capacities, instance.initialState().capacities) / instance.maximalDistance;
    }
}
