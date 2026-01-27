package org.ddolib.examples.qks;

import java.util.Arrays;

public class QKSState {

    /** Remaining capacity */
    double capacity;
    double[] itemsProfit;

    /**
     * Constructs a new QKSState with the given capacity and vector of profits for each item
     *
     * @param capacity a double
     * @param itemsProfit an array of double representing the sum of the profit linked to each item
     */
    public QKSState(double capacity, double[] itemsProfit) {
        this.capacity = capacity;
        this.itemsProfit = itemsProfit;
    }

    /**
     * Creates a deep copy of this state.
     *
     * @return a new {@code QKSState} with a cloned profit array
     */
    @Override
    public QKSState clone() {
        return new QKSState(capacity, itemsProfit.clone());
    }

    /**
     * Returns a string representation of this state.
     *
     * @return a string showing the capacity and the profit array
     */
    @Override
    public String toString() {
        return capacity + "; " + Arrays.toString(itemsProfit);
    }

    /**
     * Computes the hash code based on the capacities array.
     *
     * @return the hash code of this state
     */
    @Override
    public int hashCode() {
        return Double.hashCode(capacity) + Arrays.hashCode(itemsProfit);
    }

    /**
     * Compares this state to another object for equality.
     *
     * @param o the object to compare with
     * @return {@code true} if {@code o} is an QKSState and has identical profits and capacity; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        assert o instanceof QKSState;
        return (capacity == ((QKSState) o).capacity) && (Arrays.equals(itemsProfit, ((QKSState) o).itemsProfit));
    }
}
