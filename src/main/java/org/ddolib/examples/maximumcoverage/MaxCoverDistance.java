package org.ddolib.examples.maximumcoverage;

import org.ddolib.ddo.core.heuristics.cluster.StateDistance;
import org.ddolib.ddo.core.mdd.NodeSubProblem;
import static org.ddolib.util.DistanceUtil.weightedJaccardDistance;
import static org.ddolib.util.DistanceUtil.symmetricDifferenceDistance;

import java.util.BitSet;

import static java.lang.Math.abs;
import static java.lang.Math.max;
/**
 * Distance function for {@link MaxCoverState} used to measure similarity
 * between states in the context of the Maximum Coverage problem.
 *
 * <p>
 * This class implements {@link StateDistance} and provides several distance
 * computations:
 * <ul>
 *   <li>a distance between two states based on the covered item sets</li>
 *   <li>a distance between two search nodes combining state similarity and cost difference</li>
 *   <li>a distance between a state and the root, used for diversification purposes</li>
 * </ul>
 *
 * <p>
 * Distances are normalized with respect to the number of items in the problem
 * instance and may rely on weighted or unweighted set-based metrics.
 */
public class MaxCoverDistance implements StateDistance<MaxCoverState> {
    /** Instance of the Maximum Coverage problem. */
    MaxCoverProblem instance;
    /**
     * Constructs a distance measure associated with a given MaxCover problem instance.
     *
     * @param instance the MaxCover problem instance providing problem-specific parameters
     */
    public MaxCoverDistance(MaxCoverProblem instance) {
            this.instance = instance;
        }
    /**
     * Computes a Roger-like distance based on the size of the intersection
     * between two sets.
     *
     * <p>
     * The distance decreases as the intersection between the two sets increases.
     *
     * @param a first set
     * @param b second set
     * @return a distance value derived from the squared intersection size
     */
    private double rogerDistance(BitSet a, BitSet b) {
        BitSet tmp = (BitSet) a.clone();
        tmp.and(b);
        int intersectionSize = tmp.cardinality();
        return 50*50 - intersectionSize*intersectionSize;
    }
    /**
     * Computes a convex combination of two distance components.
     *
     * <p>
     * The combination is controlled by a fixed coefficient {@code alpha}.
     *
     * @param distanceOnSet  distance component based on state similarity
     * @param distanceOnCost distance component based on objective value difference
     * @return the combined distance
     */
    private double convexCombination(double distanceOnSet, double distanceOnCost) {
        double alpha = 0.25;
        return alpha * distanceOnCost + (1 - alpha) * distanceOnSet;
    }
    /**
     * Computes the distance between a state and the root of the search tree.
     *
     * <p>
     * This distance is proportional to the fraction of items covered by the state.
     *
     * @param state the state for which the distance to the root is computed
     * @return a normalized distance to the root
     */
    @Override
    public double distanceWithRoot(MaxCoverState state) {
            return ((double) state.coveredItems().cardinality()) /instance.nbItems;
    }
    /**
     * Computes the distance between two search nodes.
     *
     * <p>
     * The distance is a convex combination of:
     * <ul>
     *   <li>a weighted Jaccard distance between the covered item sets</li>
     *   <li>a normalized difference between the node objective values</li>
     * </ul>
     *
     * @param a first node
     * @param b second node
     * @return the combined distance between the two nodes
     */
    @Override
    public double distance(NodeSubProblem<MaxCoverState> a, NodeSubProblem<MaxCoverState> b) {
        double distanceOnSet = weightedJaccardDistance(a.state.coveredItems(), b.state.coveredItems(), instance.centralities);
        double distanceOnCost = abs(a.getValue() - b.getValue()) / instance.nbItems;
        return convexCombination(distanceOnSet, distanceOnCost);
    }
    /**
     * Computes the distance between two MaxCover states.
     *
     * <p>
     * The distance is based on the size of the symmetric difference
     * between the sets of covered items, normalized by the total number of items.
     *
     * @param a first state
     * @param b second state
     * @return a normalized symmetric difference distance
     */
    @Override
    public double distance(MaxCoverState a, MaxCoverState b) {
        return symmetricDifferenceDistance(a.coveredItems(), b.coveredItems()) / instance.nbItems;
    }


}
