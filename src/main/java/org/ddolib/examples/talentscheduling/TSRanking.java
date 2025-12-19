package org.ddolib.examples.talentscheduling;

import org.ddolib.modeling.StateRanking;
/**
 * Class that defines a ranking (ordering) between two {@link TSState} instances.
 *
 * <p>
 * This ranking is based on the total number of scenes that are either remaining
 * or marked as "maybe" in the state. States with fewer total scenes are considered
 * smaller (i.e., ranked higher) than states with more total scenes.
 * </p>
 *
 * <p>
 * This class implements the {@link StateRanking} interface and is used in DDO or
 * other search algorithms to prioritize states when merging or pruning.
 * </p>
 */
public class TSRanking implements StateRanking<TSState> {
    /**
     * Compares two {@link TSState} instances based on the total number of scenes
     * in their {@code remainingScenes} and {@code maybeScenes} sets.
     *
     * @param o1 The first state to compare.
     * @param o2 The second state to compare.
     * @return A negative integer, zero, or a positive integer as the first argument
     *         is less than, equal to, or greater than the second.
     */
    @Override
    public int compare(TSState o1, TSState o2) {
        int totalO1 = o1.remainingScenes().cardinality() + o1.maybeScenes().cardinality();
        int totalO2 = o2.remainingScenes().cardinality() + o2.maybeScenes().cardinality();
        return Integer.compare(totalO1, totalO2);
    }
}
