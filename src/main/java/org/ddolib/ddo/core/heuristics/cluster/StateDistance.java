package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;
import org.ddolib.examples.maximumcoverage.MaxCoverState;

/**
 * Interface defining a distance function between states, used to form clusters
 * when deciding which nodes on a layer of a decision diagram should be merged.
 *
 * <p>
 * The distance function must satisfy the following properties:
 * <ul>
 *   <li>Non-negative: distance(a, b) ≥ 0</li>
 *   <li>Symmetric: distance(a, b) = distance(b, a)</li>
 *   <li>Triangle inequality: distance(a, c) ≤ distance(a, b) + distance(b, c)</li>
 * </ul>
 *
 * <p>
 * Type parameter {@code T} denotes the type of the states being compared.
 *
 * @param <T> the type of states
 */
public interface StateDistance<T> {

    /**
     * Computes the discrete distance between two states.
     *
     * @param a the first state
     * @param b the second state
     * @return the distance between {@code a} and {@code b}
     */
    double distance(T a, T b);
    /**
     * Computes the distance between two nodes of a subproblem.
     *
     * <p>
     * By default, returns 0. Can be overridden for more precise node-level distances.
     *
     * @param a the first node
     * @param b the second node
     * @return the distance between the nodes
     */
    default double distance(NodeSubProblem<T> a, NodeSubProblem<T> b) {
        return 0;
    }
    /**
     * Computes the distance between a state and the root of the search/tree.
     *
     * <p>
     * By default, returns 0. Can be overridden for root-distance computations.
     *
     * @param state the state to measure
     * @return the distance to the root
     */
    default double distanceWithRoot(T state) {
        return 0;
    }

}
