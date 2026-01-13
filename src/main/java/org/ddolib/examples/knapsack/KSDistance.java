package org.ddolib.examples.knapsack;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
/**
 * Distance measure for states in a Knapsack (KS) problem.
 *
 * <p>
 * This class implements {@link StateDistance} for states represented as integers
 * (typically the current total weight of the knapsack). It provides methods to
 * compute distances between states and between a state and the root.
 *
 * <p>
 * Distances are normalized by the knapsack capacity to produce values in [0,1].
 */
public class KSDistance implements StateDistance<Integer> {
    /** The Knapsack problem instance associated with this distance. */
    final private KSProblem problem;
    /**
     * Constructs a distance measure for a given Knapsack problem instance.
     *
     * @param problem the Knapsack problem
     */
    public KSDistance(KSProblem problem) {
        this.problem = problem;
    }
    /**
     * Computes the normalized distance between two states.
     *
     * <p>
     * The distance is the absolute difference of their integer values divided
     * by the knapsack capacity.
     *
     * @param a the first state
     * @param b the second state
     * @return the normalized distance between {@code a} and {@code b}
     */
    @Override
    public double distance(Integer a, Integer b) {
        return ((double) Math.abs(a - b) / problem.capa);
    }
    /**
     * Computes the normalized distance between a state and the root (empty knapsack).
     *
     * @param state the state to measure
     * @return the normalized distance from the root
     */
    @Override
    public double distanceWithRoot(Integer state) {
        return ((double) state) / problem.capa;
    }

}
