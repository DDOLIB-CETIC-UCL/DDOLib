package org.ddolib.nolayer.solving.ddo.core.heuristics.cluster;

import java.util.List;

/**
 * Interface defining a strategy to reduce the number of states of a decision diagram frontier
 * by clustering states for restriction and relaxation.
 * <p>
 * Implementations of this interface determine how to group states into clusters
 * when the frontier exceeds a desired maximum width.
 *
 * @param <T> the type of states in the decision diagram
 */
public interface ReductionStrategy<T> {

    /**
     * Generates clusters of states for restriction and relaxation from the given states.
     *
     * @param states   the states to cluster
     * @param values   the objective value of the best path leading to each state
     * @param maxWidth the target maximum number of clusters
     * @return the resulting clusters, each cluster being a list of states
     */
    List<List<T>> defineClusters(List<T> states, List<Double> values, int maxWidth);
}
