package org.ddolib.examples.tsptw;

import java.util.BitSet;

/**
 * Represents a state in the dynamic programming model for the Traveling Salesman Problem with Time Windows (TSPTW).
 * <p>
 * Each state encapsulates the current information about the vehicle's position, the set of nodes yet to visit,
 * and timing information. This record is used both for individual states and for relaxed/merged states.
 * </p>
 *
 * @param position       The current last position of the vehicle. Usually unique and represented by {@link TSPNode}.
 *                       In merged states, the vehicle can be "at any position at the same time," represented by {@link VirtualNodes}.
 * @param time           The arrival time of the vehicle at the current position.
 * @param mustVisit      A {@link BitSet} representing all nodes that must still be visited.
 * @param possiblyVisit  A {@link BitSet} representing nodes that might have been visited or not in merged states.
 * @param depth          The depth of the layer containing this state in the dynamic programming model.
 */
public record TSPTWState(Position position, int time, BitSet mustVisit, BitSet possiblyVisit,
                         int depth) {
    /**
     * Returns a string representation of this TSPTW state, including position, time, must-visit and possibly-visit nodes, and depth.
     *
     * @return a formatted string describing the state
     */
    @Override
    public String toString() {
        return String.format("position: %s - time: %d - must visit: %s - possibly visit: %s - depth: %d",
                position,
                time,
                mustVisit,
                possiblyVisit,
                depth);
    }
}
