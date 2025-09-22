package org.ddolib.ddo.core.profiling;

public class Pair {
    double gap;
    long runTime;
    public Pair(double gap, long runTime) {
        this.gap = gap;
        this.runTime = runTime;
    }
    @Override
    public String toString() {
        return "("+gap+","+runTime+")";
    }
}
