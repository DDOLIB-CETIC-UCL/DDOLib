package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;
import org.ddolib.examples.maximumcoverage.MaxCoverState;

/**
 * This abstraction defines the distance function used to constitute the
 * cluster when deciding which nodes on a layer should be merged.
 * This function distance must:
 *  - be non-negative
 *  - be symmetric
 *  - obey the triangular inequality
 *
 * Again, the type parameter `T` denotes the type of the states.
 */
public interface StateDistance<T> {

    /**
     * Computes the discrete distance between the two given states
     * @param a the first state
     * @param b the second state
     * @return the distance between them
     */
    double distance(T a, T b);

    default double distance(NodeSubProblem<T> a, NodeSubProblem<T> b) {
        return 0;
    }

    default double distanceWithRoot(T state) {
        return 0;
    }

}
