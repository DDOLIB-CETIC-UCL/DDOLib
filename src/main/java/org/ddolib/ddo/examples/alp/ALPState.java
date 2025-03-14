package org.ddolib.ddo.examples.alp;

import java.util.Arrays;
import java.util.Objects;

/** State of the ALP problem at a given time.
 * <p>
 * Basically the remaining aircraft and the state of each runway.
 */
public class ALPState {

    // Remaining aircraft for each class.
    public int[] remainingAircraftOfClass;
    // The state of each runway.
    public RunwayState[] runwayStates;

    public ALPState(int[] remainingAircraft, RunwayState[] runwayStates){
        this.remainingAircraftOfClass = remainingAircraft;
        this.runwayStates = runwayStates;
    }

    public ALPState(ALPState other){
        int runwayStatesLength = other.runwayStates.length;
        int remLength = other.remainingAircraftOfClass.length;
        remainingAircraftOfClass = new int[remLength];
        System.arraycopy(other.remainingAircraftOfClass, 0, remainingAircraftOfClass, 0, remLength);
        runwayStates = new RunwayState[runwayStatesLength];
        for(int i = 0; i < runwayStatesLength; i++)
            runwayStates[i] = new RunwayState(other.runwayStates[i]);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ALPState alpState = (ALPState) o;

        return Arrays.equals(alpState.runwayStates,this.runwayStates) && Arrays.equals(alpState.remainingAircraftOfClass,this.remainingAircraftOfClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(remainingAircraftOfClass), Arrays.hashCode(runwayStates));
    }

    @Override
    public String toString() {
        return "Runway states : " + Arrays.toString(runwayStates) + "\nRemaining air crafts : " + Arrays.toString(remainingAircraftOfClass);
    }
}
