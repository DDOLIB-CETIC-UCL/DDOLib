package org.ddolib.examples.pdptw;

public record TimeWindow(int start, int end) {
    public int entryTime(int arrivalTime){
        return Math.max(arrivalTime, start);
    }
    public int waitTime(int arrivalTime){
        return Math.max(arrivalTime, start) - arrivalTime;
    }
}
