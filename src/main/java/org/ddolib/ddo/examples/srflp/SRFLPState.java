package org.ddolib.ddo.examples.srflp;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

public record SRFLPState(BitSet remaining, BitSet maybe, int[] cut, int depth) {

    @Override
    public String toString() {
        return String.format("State: remaining: %s - maybe: %s - cut: %s", remaining, maybe, Arrays.toString(cut));
    }

    @Override
    public int hashCode() {
        return Objects.hash(remaining, maybe, Arrays.hashCode(cut), depth);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SRFLPState(BitSet r, BitSet m, int[] c, int d)) {
            return remaining.equals(r) && maybe.equals(m) && Arrays.equals(cut, c) && depth == d;
        } else {
            return false;
        }
    }
}
