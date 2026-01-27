package org.ddolib.examples.mks;

import java.util.Arrays;
/**
 * Represents the state of a multi-dimensional Knapsack problem (MKS) in terms of
 * the remaining capacities of each knapsack dimension.
 *
 * <p>
 * This class encapsulates the capacities as a double array and provides standard
 * methods for cloning, equality checking, and string representation.
 */
public class MKSState {
    /** Remaining capacities of each knapsack dimension. */
    double[] capacities;
    /**
     * Constructs a new MKSState with the given capacities.
     *
     * @param capacities an array representing the remaining capacities of each knapsack dimension
     */
    public MKSState(double[] capacities) {
        this.capacities = capacities;
    }
    /**
     * Creates a deep copy of this state.
     *
     * @return a new {@code MKSState} with a cloned capacities array
     */
    @Override
    public MKSState clone() {
        return new MKSState(capacities.clone());
    }
    /**
     * Returns a string representation of this state.
     *
     * @return a string showing the capacities array
     */
    @Override
    public String toString() {
        return Arrays.toString(capacities);
    }
    /**
     * Computes the hash code based on the capacities array.
     *
     * @return the hash code of this state
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(capacities);
    }
    /**
     * Compares this state to another object for equality.
     *
     * @param o the object to compare with
     * @return {@code true} if {@code o} is an MKSState and has identical capacities; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        assert o instanceof MKSState;
        return Arrays.equals(capacities, ((MKSState) o).capacities);
    }

}
