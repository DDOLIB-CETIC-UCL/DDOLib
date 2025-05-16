package org.ddolib.ddo.examples.srflp;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

public record SRFLPState(BitSet must, BitSet maybe, int[] cut, int depth) {

    @Override
    public String toString() {
        return String.format("State: must: %s - maybe: %s - cut: %s - depth: %d",
                must, maybe, Arrays.toString(cut), depth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(must, maybe, Arrays.hashCode(cut), depth);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SRFLPState(BitSet otherMust, BitSet otherMaybe, int[] otherCut, int otherDepth)) {
            return this.must.equals(otherMust)
                    && this.maybe.equals(otherMaybe)
                    && Arrays.equals(this.cut, otherCut)
                    && this.depth == otherDepth;
        } else {
            return false;
        }
    }
}
