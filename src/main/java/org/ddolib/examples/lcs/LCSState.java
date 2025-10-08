package org.ddolib.examples.lcs;

import java.util.Arrays;

/**
 * The state of a node in the LCS problem is simply the current position in each string.
 */
public class LCSState {
    int[] position;

    LCSState(int[] position) {
        this.position = position;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(position);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LCSState other) {
            return Arrays.equals(this.position, other.position);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(position);
    }
}
