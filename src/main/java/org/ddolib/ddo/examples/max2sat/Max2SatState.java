package org.ddolib.ddo.examples.max2sat;

import java.util.ArrayList;
import java.util.Objects;

public record Max2SatState(ArrayList<Integer> netBenefit, int depth) {

    @Override
    public String toString() {
        return netBenefit.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Max2SatState(ArrayList<Integer> otherBenefit, int otherDepth)) {
            return this.depth == otherDepth && this.netBenefit == otherBenefit;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(netBenefit, depth);
    }
}
