package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
/**
 * A simple random-based reduction strategy for decision diagram layers.
 *
 * <p>
 * This class implements {@link ReductionStrategy} and generates clusters
 * by randomly selecting nodes from a layer. Each selected node forms its own cluster.
 *
 * <p>
 * The strategy is controlled by a {@link Random} object, which can be seeded
 * to ensure reproducible behavior.
 *
 * @param <T> the type of states in the decision diagram
 */
public class RandomBased<T> implements ReductionStrategy<T> {
    /** Random number generator used for shuffling nodes. */
    final private Random rnd;
    /**
     * Constructs a random-based reduction strategy with a given seed.
     *
     * @param seed the seed for the random number generator
     */
    public RandomBased(long seed) {
        this.rnd = new Random(seed);
    }
    /**
     * Defines clusters by randomly selecting up to {@code maxWidth} nodes
     * from the layer. Each selected node forms a separate cluster.
     *
     * <p>
     * The nodes selected for clustering are removed from the input {@code layer}.
     *
     * @param layer the list of nodes at the current layer
     * @param maxWidth the maximum number of clusters (and nodes kept)
     * @return an array of clusters, each cluster being a list containing a single node
     */
    @Override
    public List<NodeSubProblem<T>>[] defineClusters(List<NodeSubProblem<T>> layer, int maxWidth) {
        List<NodeSubProblem<T>>[] clusters = new List[maxWidth];
        Collections.shuffle(layer, rnd);
        List<NodeSubProblem<T>> kept = layer.subList(0, maxWidth);
        for (int i = 0; i < maxWidth; i++) {
            clusters[i] = new ArrayList<>();
            clusters[i].add(kept.get(i));
        }
        return clusters;
    }
    /**
     * Resets the seed of the random number generator.
     *
     * @param seed the new seed value
     */
    public void setSeed(long seed) {
        this.rnd.setSeed(seed);
    }
}
