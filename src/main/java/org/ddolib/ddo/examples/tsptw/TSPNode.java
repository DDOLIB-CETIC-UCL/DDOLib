package org.ddolib.ddo.examples.tsptw;

public record TSPNode(int value) implements Position {
    @Override
    public String toString() {
        return "" + value;
    }
}
