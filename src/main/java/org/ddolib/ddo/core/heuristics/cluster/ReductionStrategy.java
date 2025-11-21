package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;

import java.util.List;

public interface ReductionStrategy<T> {

    /**
     * Generates cluster of nodes for restriction and relaxation from the given layer.
     * All nodes added to a cluster are removed from the given layer.
     * @param layer the layer
     * @param maxWidth the desired maximal width after the restriction and relaxation
     * @return an array of clusters represented as List
     */
    public List<NodeSubProblem<T>>[] defineClusters(final List<NodeSubProblem<T>> layer, final int maxWidth);

    public void setSeed(long seed);
}
