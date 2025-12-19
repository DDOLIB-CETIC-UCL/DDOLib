package org.ddolib.examples.tsp;

import org.ddolib.modeling.StateRanking;
/**
 * Class that defines a ranking between two {@link TSPState} instances.
 *
 * <p>
 * This class implements the {@link StateRanking} interface for TSP states.
 * It is intended to provide a comparison method between states, which can be used
 * to prioritize or order states in search algorithms such as DDO or A*.
 * </p>
 *
 * <p>
 * Currently, the {@link #compare(TSPState, TSPState)} method returns 0 for all states,
 * indicating that all states are considered equal in terms of ranking.
 * This can be customized to implement a meaningful heuristic-based ranking.
 * </p>
 *
 * @see TSPState
 * @see StateRanking
 */
public class TSPRanking implements StateRanking<TSPState> {
    /**
     * Compares two TSP states.
     *
     * @param o1 the first state
     * @param o2 the second state
     * @return 0 as a placeholder, indicating that the two states are equal in ranking
     */
    @Override
    public int compare(final TSPState o1, final TSPState o2) {
        return 0;
    }
}
