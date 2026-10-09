package org.ddolib.examples.layered.tsptw;

/**
 * Unique position of the vehicle.
 *
 * @param value last position of the vehicle in the current route
 */
record TSPNode(int value) implements Position {
    @Override
    public String toString() {
        return "" + value;
    }
}
