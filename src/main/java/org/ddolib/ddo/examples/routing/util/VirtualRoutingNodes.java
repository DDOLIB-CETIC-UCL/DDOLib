package org.ddolib.ddo.examples.routing.util;

import java.util.Set;

/**
 * Used for merged states. The vehicle can be at all the position of the merged states.
 *
 * @param nodes All the position of the merged states.
 */
public record VirtualRoutingNodes(Set<Integer> nodes) implements RoutePosition {
    @Override
    public String toString() {
        return nodes.toString();
    }
}
