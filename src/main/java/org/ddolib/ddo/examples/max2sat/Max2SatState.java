package org.ddolib.ddo.examples.max2sat;

public class Max2SatState {
    public final int[] marginalCosts;
    public final int depth;

    public Max2SatState(int[] marginalCosts, int depth) {
        this.marginalCosts = marginalCosts;
        this.depth = depth;
    }
}
