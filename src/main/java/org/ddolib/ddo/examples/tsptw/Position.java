package org.ddolib.ddo.examples.tsptw;

import java.util.Set;

/**
 * Interface to model the position of the vehicle in a {@link TSPTWState}.
 */
public sealed interface Position permits TSPNode, VirtualNodes {
}


/**
 * Unique position of the vehicle.
 *
 * @param value Last position of the vehicle in the current route.
 */
record TSPNode(int value) implements Position {
    @Override
    public String toString() {
        return "" + value;
    }
}


/**
 * Used for merged states. The vehicle can be at all the position of the merged states.
 *
 * @param nodes All the position of the merged states.
 */
record VirtualNodes(Set<Integer> nodes) implements Position {
}

