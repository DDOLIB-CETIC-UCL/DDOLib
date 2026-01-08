package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;
import org.ddolib.modeling.StateRanking;

import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.ceil;
/**
 * Hybrid reduction strategy that combines cost-based and distance-based clustering
 * for decision diagram layers.
 * This strategy is a hybridation between cost based selection and GHP.
 * It preserves the w * alpha best nodes (alpha between 0 and 1) and merge the other nodes using clustering.
 * It requires a problem-specific StateRanking comparator to break the ties between nodes of same cost,
 * and a problem-specif StateDistance to quantify the dissimilarity between states.
 * <p>
 * This class implements {@link ReductionStrategy} and merges two strategies:
 * <ul>
 *   <li>{@link CostBased}: preserves a fraction of nodes based on their ranking</li>
 *   <li>{@link GHP}: clusters the remaining nodes based on a distance metric</li>
 * </ul>
 *
 * <p>
 * The combination is controlled by a weighting parameter {@code alpha}:
 * <ul>
 *   <li>{@code alpha} fraction of the clusters are preserved using cost-based ranking</li>
 *   <li>{@code 1-alpha} fraction of the clusters are formed using the GHP distance-based method</li>
 * </ul>
 *
 * @param <T> the type of states in the decision diagram
 */
public class Hybrid<T> implements ReductionStrategy<T> {
    /** Cost-based clustering component. */
    final private CostBased<T> costBased;
    /** Distance-based clustering component (GHP). */
    final private GHP<T> ghp;
    /** Fraction of clusters preserved using the cost-based method. */
    final private double alpha;
    /**
     * Constructs a Hybrid reduction strategy with specified ranking, distance, alpha, and seed.
     *
     * @param ranking state ranking used for cost-based preservation
     * @param distance state distance used for GHP clustering
     * @param alpha fraction of clusters preserved using cost-based strategy
     * @param seed random seed for distance-based clustering
     */
    public Hybrid(final StateRanking<T> ranking, final StateDistance<T> distance, final double alpha, final long seed) {
        this.costBased = new CostBased<>(ranking);
        this.ghp = new GHP<>(distance, seed);
        this.alpha = alpha;
    }
    /**
     * Constructs a Hybrid reduction strategy with default alpha (0.5) and seed.
     *
     * @param ranking state ranking used for cost-based preservation
     * @param distance state distance used for GHP clustering
     */
    public Hybrid(final StateRanking<T> ranking, final StateDistance<T> distance) {
        this(ranking, distance, 0.5, 465465);
    }
    /**
     * Sets the random seed used for the distance-based GHP clustering.
     *
     * @param seed the new random seed
     */
    public void setSeed(long seed) {
        this.ghp.setSeed(seed);
    }
    /**
     * Defines clusters from a layer of nodes using a hybrid strategy.
     *
     * <p>
     * A fraction {@code alpha} of nodes are clustered using cost-based ranking,
     * while the remaining nodes are clustered using the GHP distance-based method.
     *
     * @param layer the list of nodes at the current layer
     * @param maxWidth the desired maximum width after clustering
     * @return an array of clusters, each cluster being a list of nodes
     */
    @Override
    public List<NodeSubProblem<T>>[] defineClusters(List<NodeSubProblem<T>> layer, int maxWidth) {
        int nbPreserved = (int) ceil(maxWidth*alpha);

        List<List<NodeSubProblem<T>>> clusters = new ArrayList<List<NodeSubProblem<T>>>();
        List<NodeSubProblem<T>>[] costClusters = costBased.defineClusters(layer, nbPreserved+1);
        for (int i = 0; i < costClusters.length-1; i++) {
            clusters.add(costClusters[i]);
        }
        List<NodeSubProblem<T>> toCluster = costClusters[nbPreserved];
        int nbClusters = maxWidth - nbPreserved;
        List<NodeSubProblem<T>>[] ghpClusters = ghp.defineClusters(toCluster, nbClusters);
        for (int i = 0; i < ghpClusters.length; i++) {
            clusters.add(ghpClusters[i]);
        }
        return clusters.toArray(new List[clusters.size()]);
    }

}
