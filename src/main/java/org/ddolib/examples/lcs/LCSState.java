package org.ddolib.examples.lcs;

import java.util.Arrays;

/**
 * Represents the state of a node in the Longest Common Subsequence (LCS) problem.
 * <p>
 * In this problem, the state is defined by the current position in each of the strings being compared.
 * Each position indicates how many characters of that string have been processed.
 * </p>
 * <p>
 * This state is used by search and optimization algorithms to track progress along the strings.
 * It is immutable in the sense that new states are created rather than modifying existing ones.
 * </p>
 */
public class LCSState {
    /**
     * Current positions in each string.
     * position[i] is the index of the next character to be considered in the i-th string.
     */
    int[] position;
    /**
     * Constructs an LCS state with the given positions for each string.
     *
     * @param position An array of integers representing the current position in each string.
     */
    LCSState(int[] position) {
        this.position = position;
    }
    /**
     * Returns a hash code for this state, based on the positions in all strings.
     *
     * @return hash code of the state.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(position);
    }
    /**
     * Checks equality between this state and another object.
     * Two states are equal if their position arrays are identical.
     *
     * @param obj The object to compare with.
     * @return true if the other object is an LCSState with the same positions; false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LCSState other) {
            return Arrays.equals(this.position, other.position);
        } else {
            return false;
        }
    }
    /**
     * Returns a string representation of the state.
     * <p>
     * The positions in each string are shown as an array.
     * </p>
     *
     * @return String representation of the state.
     */

    @Override
    public String toString() {
        return Arrays.toString(position);
    }
}
