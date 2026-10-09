package org.ddolib.examples.layered.tsptw;

import java.util.Set;

/**
 * Used for merged states. The vehicle can be at all the position of the merged states.
 *
 * @param nodes all the position of the merged states
 */
record VirtualNodes(Set<Integer> nodes) implements Position {
    @Override
    public String toString() {
        return nodes.toString();
    }
}
