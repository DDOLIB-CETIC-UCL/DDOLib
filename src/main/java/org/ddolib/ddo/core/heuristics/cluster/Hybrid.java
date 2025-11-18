package org.ddolib.ddo.core.heuristics.cluster;

import org.ddolib.ddo.core.mdd.NodeSubProblem;
import org.ddolib.modeling.StateRanking;

import java.util.List;
import static java.lang.Math.ceil;

/**
 * This strategy is a hybridation between cost based selection and GHP.
 * It preserves the w*alpha best nodes (0 <= alpha <= 1) and merge the other nodes using clustering.
 * It requires a problem-specific StateRanking comparator to break the ties between nodes of same cost,
 * and a problem-specif StateDistance to quantify the dissimilarity between states.
 * @param <T>
 */
public class Hybrid<T> implements ReductionStrategy<T> {
    final private CostBased<T> costBased;
    final private GHP<T> ghp;
    final private double alpha;

    public Hybrid(final StateRanking<T> ranking, final StateDistance<T> distance, final double alpha) {
        this.costBased = new CostBased<>(ranking);
        this.ghp = new GHP<>(distance);
        this.alpha = alpha;
    }

    public Hybrid(final StateRanking<T> ranking, final StateDistance<T> distance) {
        this(ranking, distance, 0.5);
    }

    public void setSeed(long seed) {
        this.ghp.setSeed(seed);
    }

    @Override
    public List<NodeSubProblem<T>>[] defineClusters(List<NodeSubProblem<T>> layer, int maxWidth) {
        int nbPreserved = (int) ceil(maxWidth*alpha);
        int nbClusters = maxWidth - nbPreserved;
        List<NodeSubProblem<T>>[] costClusters = costBased.defineClusters(layer, nbPreserved+1);
        assert costClusters.length == 1;
        List<NodeSubProblem<T>>[] clusters = ghp.defineClusters(costClusters[0], nbClusters);
        assert layer.size() == nbPreserved;
        assert clusters.length == nbClusters;
        assert layer.size() + clusters.length == maxWidth;
        return clusters;
    }

    @Override
    public String toString() {
        return "Hybrid";
    }

}
