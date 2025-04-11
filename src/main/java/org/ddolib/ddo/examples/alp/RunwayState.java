package org.ddolib.ddo.examples.alp;

import java.util.Objects;

/** State of a runway : last landing time and last aircraft's class */
public class RunwayState implements Comparable<RunwayState>{
    // Previous landing time
    public int prevTime;
    // Previous class of aircraft that landed
    public int prevClass;

    public RunwayState(int prevClass, int prevTime){
        this.prevClass = prevClass;
        this.prevTime = prevTime;
    }

    public RunwayState(RunwayState other){
        prevTime = other.prevTime;
        prevClass = other.prevClass;
    }

    @Override
    public int compareTo(RunwayState o) {
        return Integer.compare(prevTime,o.prevTime);
    }

    @Override
    public String toString() {
        return "PT " + prevTime + " PC : " + prevClass;
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
