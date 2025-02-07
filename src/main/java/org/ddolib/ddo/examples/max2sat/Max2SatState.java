package org.ddolib.ddo.examples.max2sat;

import java.util.Arrays;

public record Max2SatState(int[] marginalCosts, int depth) {

    public int rank() {
        return Arrays.stream(marginalCosts).map(Math::abs).sum();
    }

    @Override
    public String toString() {
        return String.format("%s - depth: %d", Arrays.toString(marginalCosts), depth);
    }
}
