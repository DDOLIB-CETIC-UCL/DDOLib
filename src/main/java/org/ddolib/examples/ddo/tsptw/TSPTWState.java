package org.ddolib.examples.ddo.tsptw;

import java.util.BitSet;

/**
 * State of ta DP-model for the TSPTW
 *
 * @param position      The current last position of the vehicle. Most of the time this position is unique and
 *                      modeled by {@link TSPNode}. Therefore, in the merged states, vehicle can be "at any position
 *                      at the same time", modeled by {@link VirtualNodes}.
 * @param time          The time at which the vehicle reached its position.
 * @param mustVisit     All nodes yet to be visited.
 * @param possiblyVisit From merged node, contains the nodes that might be visited previously or not.
 * @param depth         The depth of the layer containing the state.
 */
public record TSPTWState(Position position, int time, BitSet mustVisit, BitSet possiblyVisit,
                         int depth) {

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
