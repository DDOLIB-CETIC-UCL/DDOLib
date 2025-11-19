package org.ddolib.examples.pigmentscheduling;

import java.util.Arrays;
import java.util.Objects;
/**
 * Represents a state in the Production Scheduling Problem (PSP).
 * <p>
 * Each {@code PSState} captures the scheduling situation at a given time slot,
 * including which item is scheduled next and the remaining demands for all item types.
 * This class is immutable in practice, with the exception of cloning for safe transitions.
 * </p>
 *
 * <p>
 * Fields:
 * </p>
 * <ul>
 *     <li>{@code t}: the current time slot (counting down from the horizon).</li>
 *     <li>{@code next}: the item type scheduled for production at time {@code t+1}.
 *         A value of {@code -1} indicates that the next item is not yet assigned.</li>
 *     <li>{@code previousDemands}: an array where {@code previousDemands[i]} stores
 *         the latest time before {@code t} when a demand for item {@code i} occurs.
 *         A value of {@code -1} indicates that there are no remaining demands for that item.</li>
 * </ul>
 * <p>
 * This class overrides {@link #hashCode()}, {@link #equals(Object)}, and {@link #toString()}
 * to allow proper usage in hash-based collections, state comparison, and debugging output.
 * </p>
 *
 * <p>
 * {@code PSState} is used in combination with {@link PSProblem}, {@link PSRelax}, and
 * {@link PSFastLowerBound} in Dynamic Decision Optimization (DDO) or A* search frameworks.
 * </p>
 *
 * @see PSProblem
 * @see PSRelax
 * @see PSFastLowerBound
 */
public class PSState {

    /** Current time slot in the scheduling horizon. */
    int t;

    /** The item type to be produced at time t+1; -1 indicates unknown or idle. */
    int next;

    /** Array storing for each item type the latest time before t with demand. */
    int[] previousDemands;

    /**
     * Constructs a new state for the PSP.
     *
     * @param t the current time slot
     * @param next the next item type scheduled for production, -1 if unknown
     * @param previousDemands an array indicating, for each item type, the last time a demand occurs before t
     */

    public PSState(int t, int next, int[] previousDemands) {
        this.t = t;
        this.next = next;
        this.previousDemands = previousDemands;
    }
    /**
     * Creates a deep copy of this state.
     *
     * @return a cloned {@code PSState} instance
     */
    @Override
    protected PSState clone() {
        return new PSState(t, next, Arrays.copyOf(previousDemands, previousDemands.length));
    }
    /**
     * Computes a hash code for the state, based on time, next item, and previous demands.
     *
     * @return the hash code of the state
     */
    @Override
    public int hashCode() {
        return Objects.hash(t, next, Arrays.hashCode(previousDemands));
    }
    /**
     * Compares this state with another object for equality.
     * Two {@code PSState} instances are equal if they have the same time,
     * next item, and previous demand array.
     *
     * @param obj the object to compare
     * @return {@code true} if the states are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PSState other) {
            return this.t == other.t
                    && this.next == other.next
                    && Arrays.equals(this.previousDemands, other.previousDemands);
        } else {
            return false;
        }
    }
    /**
     * Returns a string representation of the state, including current time,
     * next item, and previous demands for debugging purposes.
     *
     * @return a string describing the state
     */
    @Override
    public String toString() {
        return String.format("t: %d - next: %d - previousDemand: %s", t, next, Arrays.toString(previousDemands));
    }
}
