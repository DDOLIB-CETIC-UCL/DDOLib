package org.ddolib.examples.tsptw;

import org.ddolib.modeling.StateRanking;
/**
 * Ranking class for states in the Traveling Salesman Problem with Time Windows (TSPTW).
 *
 * <p>
 * This class implements {@link StateRanking} for {@link TSPTWState} and is used to order
 * states within the same layer of a decision diagram. The ranking helps identify which
 * states are better candidates for merging in a relaxed decision diagram.
 * </p>
 *
 * <p>
 * The comparison is based on the number of nodes in the {@code possiblyVisit} set:
 * states with more nodes in {@code possiblyVisit} are considered better candidates for merging
 * and are ranked higher.
 * </p>
 */
public class TSPTWRanking implements StateRanking<TSPTWState> {
    /**
     * Compares two {@link TSPTWState} objects based on the size of their {@code possiblyVisit} set.
     *
     * @param o1 the first state to compare
     * @param o2 the second state to compare
     * @return a negative integer, zero, or a positive integer if the first state has more, equal,
     *         or fewer nodes in {@code possiblyVisit} than the second state, respectively.
     */
    @Override
    public int compare(TSPTWState o1, TSPTWState o2) {
        // In a layer,nodes with a non-empty possiblyVisit are children of a merged node.
        // There are good candidates to be merged.
        return -Integer.compare(o1.possiblyVisit().cardinality(), o2.possiblyVisit().cardinality());
    }
}
