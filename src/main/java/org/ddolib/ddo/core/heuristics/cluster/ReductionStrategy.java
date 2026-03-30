package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;

import java.util.List;
/**
 * Interface defining a strategy to reduce the number of nodes in a layer of a decision diagram
 * by clustering nodes for restriction and relaxation.
 *
 * <p>
 * Implementations of this interface determine how to group nodes into clusters
 * when the layer exceeds a desired maximum width. All nodes assigned to clusters
 * are removed from the original layer.
 *
 * <p>
 * Type parameter {@code T} denotes the type of states associated with the nodes.
 *
 * @param <T> the type of states in the decision diagram
 */
public interface ReductionStrategy<T> {

    /**
     * Generates clusters of nodes for restriction and relaxation from the given layer.
     *
     * <p>
     * Each cluster is represented as a {@link List} of {@link NodeSubProblem} objects.
     * All nodes included in clusters are removed from the input {@code layer}.
     *
     * @param layer the list of nodes at the current layer
     * @param maxWidth the target maximum width of the layer after reduction
     * @return an array of clusters, each cluster being a list of nodes
     */
    public List<NodeSubProblem<T>>[] defineClusters(final List<NodeSubProblem<T>> layer, final int maxWidth);
}
