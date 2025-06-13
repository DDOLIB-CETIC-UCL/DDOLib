package org.ddolib.ddo.examples.srflp;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

/**
 * Represents a state for a SRFLP instance.
 *
 * @param must  The set of the remaining department that must be placed.
 * @param maybe Used by merged nodes. Contains department that must be placed for some of the merged nodes but that
 *              has already been placed for other ones.
 * @param cut   For each free department, contains the sum of all traffic intensities from the fixed departments and
 *              each free department.
 * @param depth The depth of the state in the associated MDD.
 */
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
