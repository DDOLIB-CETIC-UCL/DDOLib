package org.ddolib.ddo.examples.TSPTW;

public record TSPNode(int value) implements Position {
    @Override
    public String toString() {
        return "" + value;
    }
}
