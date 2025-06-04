package org.ddolib.ddo.examples.routing.util;

/**
 * Unique position of the vehicle.
 *
 * @param value Last position of the vehicle in the current route.
 */
public record RoutingNode(int value) implements RoutePosition {
    @Override
    public String toString() {
        return "" + value;
    }
}
