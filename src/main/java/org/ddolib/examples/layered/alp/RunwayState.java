package org.ddolib.examples.layered.alp;

import java.util.Objects;

/**
 * State of a runway : last landing time and last aircraft's class
 */
public class RunwayState implements Comparable<RunwayState> {
    /**
     * The previous landing time on this runway.
     */
    public int prevTime;
    /**
     * The class of the previous aircraft that landed on this runway.
     */
    public int prevClass;

    /**
     * Creates a new runway state.
     *
     * @param prevClass the class of the previous aircraft that landed
     * @param prevTime  the previous landing time
     */
    public RunwayState(int prevClass, int prevTime) {
        this.prevClass = prevClass;
        this.prevTime = prevTime;
    }

    /**
     * Creates a copy of the given runway state.
     *
     * @param other the runway state to copy
     */
    public RunwayState(RunwayState other) {
        prevTime = other.prevTime;
        prevClass = other.prevClass;
    }

    @Override
    public int compareTo(RunwayState o) {
        return Integer.compare(prevTime, o.prevTime);
    }

    @Override
    public String toString() {
        return "PT: " + prevTime + " PC : " + prevClass;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RunwayState that = (RunwayState) o;
        return prevTime == that.prevTime && prevClass == that.prevClass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(prevTime, prevClass);
    }
}
