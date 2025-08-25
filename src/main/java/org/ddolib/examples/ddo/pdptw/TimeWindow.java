package org.ddolib.examples.ddo.pdptw;

public record TimeWindow(int start, int end) {
    public int entryTime(int arrivalTime){
        return Math.max(arrivalTime, start);
    }
}
