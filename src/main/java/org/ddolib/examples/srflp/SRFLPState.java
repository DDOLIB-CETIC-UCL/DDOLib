package org.ddolib.examples.srflp;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

/**
 * Represents a state in a Single Row Facility Layout Problem (SRFLP) instance.
 *
 * <p>
 * Each state keeps track of which departments must still be placed, which departments
 * are optional (in case of merged nodes), the cumulative cut values, and the depth
 * in the decision diagram or search tree.
 * </p>
 *
 * @param must  The set of departments that must still be placed in all possible completions
 *              of the current state. Represented as a {@link BitSet}.
 * @param maybe The set of departments that must be placed for some of the merged states,
 *              but may have already been placed in others. Used primarily in relaxed/merged states.
 * @param cut   An array containing, for each free department, the sum of all traffic intensities
 *              from the fixed departments to that department.
 * @param depth The depth of the state in the associated decision diagram or search tree.
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
        if (obj instanceof SRFLPState(
                BitSet otherMust, BitSet otherMaybe, int[] otherCut, int otherDepth
        )) {
            return this.must.equals(otherMust)
                    && this.maybe.equals(otherMaybe)
                    && Arrays.equals(this.cut, otherCut)
                    && this.depth == otherDepth;
        } else {
            return false;
        }
    }
}
