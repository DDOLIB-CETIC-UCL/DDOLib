package org.ddolib.examples.alp;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the state of the Aircraft Landing Problem (ALP) at a given moment.
 * <p>
 * An {@code ALPState} encapsulates:
 * </p>
 * <ul>
 *     <li>The number of remaining aircraft to land for each aircraft class.</li>
 *     <li>The current state of each runway, including the class of the last landed aircraft
 *         and its landing time.</li>
 * </ul>
 * This state is used by decision diagram solvers to track the progress of the landing
 * schedule and compute feasible transitions.
 *
 * <p>
 * Instances of this class are immutable through the copy constructor and can be
 * compared using {@link #equals(Object)} and {@link #hashCode()}.
 * </p>
 */
public class ALPState {

    /** Number of remaining aircraft for each class. */
    public int[] remainingAircraftOfClass;
    /** State of each runway, including last landed aircraft class and landing time. */
    public RunwayState[] runwayStates;
    /**
     * Constructs a new ALP state with the given remaining aircraft and runway states.
     *
     * @param remainingAircraft the array representing remaining aircraft per class
     * @param runwayStates the array representing the state of each runway
     */
    public ALPState(int[] remainingAircraft, RunwayState[] runwayStates) {
        this.remainingAircraftOfClass = remainingAircraft;
        this.runwayStates = runwayStates;
    }
    /**
     * Copy constructor: creates a deep copy of another {@code ALPState}.
     *
     * @param other the state to copy
     */
    public ALPState(ALPState other) {
        int runwayStatesLength = other.runwayStates.length;
        int remLength = other.remainingAircraftOfClass.length;
        remainingAircraftOfClass = new int[remLength];
        System.arraycopy(other.remainingAircraftOfClass, 0, remainingAircraftOfClass, 0, remLength);
        runwayStates = new RunwayState[runwayStatesLength];
        for (int i = 0; i < runwayStatesLength; i++)
            runwayStates[i] = new RunwayState(other.runwayStates[i]);
    }
    /**
     * Checks if two ALP states are equal.
     * <p>
     * Two states are considered equal if they have the same remaining aircraft per class
     * and identical runway states.
     * </p>
     *
     * @param o the object to compare with
     * @return {@code true} if the states are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ALPState alpState = (ALPState) o;

        return Arrays.equals(alpState.runwayStates, this.runwayStates) && Arrays.equals(alpState.remainingAircraftOfClass, this.remainingAircraftOfClass);
    }
    /**
     * Computes the hash code for this state, based on remaining aircraft and runway states.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(remainingAircraftOfClass), Arrays.hashCode(runwayStates));
    }
    /**
     * Returns a string representation of the ALP state.
     *
     * @return a string showing the runway states and remaining aircraft per class
     */
    @Override
    public String toString() {
        return "Runway states : " + Arrays.toString(runwayStates) + "\nRemaining air crafts : " + Arrays.toString(remainingAircraftOfClass);
    }
}
