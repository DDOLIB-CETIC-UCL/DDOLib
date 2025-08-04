package org.ddolib.examples.ddo.lcs;

/**
 * The state of a node in the LCS problem is simply the current position in each string.
 */
public class LCSState {
    int[] position;

    LCSState(int[] position) {
        this.position = position;
    }
}
