package org.ddolib.examples.pdptw;

/**
 * Time window with an opening and closing time.
 *
 * @param start earliest admissible service time
 * @param end latest admissible service time
 */
public record TimeWindow(double start, double end) {
    /**
     * Returns the effective service entry time for a given arrival.
     *
     * @param arrivalTime arrival time at the node
     * @return max(arrivalTime, start)
     */
    public double entryTime(double arrivalTime){
        return Math.max(arrivalTime, start);
    }

    /**
     * Returns the waiting time incurred when arriving before opening.
     *
     * @param arrivalTime arrival time at the node
     * @return non-negative waiting duration
     */
    public double waitTime(double arrivalTime){
        return Math.max(arrivalTime, start) - arrivalTime;
    }
}
