package org.ddolib.ddo.examples.max2sat;

import java.util.Arrays;

public record Max2SatState(int[] netBenefit) {

    public int rank() {
        return Arrays.stream(netBenefit).map(Math::abs).sum();
    }

    @Override
    public String toString() {
        return String.format("%s - hashcode: %d", Arrays.toString(netBenefit), hashCode());
    }

    @Override
    public int hashCode() {
        return Arrays.stream(netBenefit).sum();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Max2SatState state) return this.netBenefit == state.netBenefit;
        else return false;
    }
}
