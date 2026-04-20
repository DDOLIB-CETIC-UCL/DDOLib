package org.ddolib.examples.pdptw;

public record TimeWindow(double start, double end) {
    public double entryTime(double arrivalTime){
        return Math.max(arrivalTime, start);
    }
    public double waitTime(double arrivalTime){
        return Math.max(arrivalTime, start) - arrivalTime;
    }
}
