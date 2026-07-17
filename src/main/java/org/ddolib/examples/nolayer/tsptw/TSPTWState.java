package org.ddolib.examples.nolayer.tsptw;

import java.util.BitSet;

/**
 * State for the NoLayer formulation of the Traveling Salesman Problem with Time Windows.
 *
 * @param currentCity the index of the current city the vehicle is located at
 * @param time        the current arrival time at the current city
 * @param mustVisit   the set of remaining cities that must be visited
 */
public record TSPTWState(int currentCity, int time, BitSet mustVisit) {
    /**
     * Defensively copies {@code mustVisit} so the state cannot be mutated through a
     * reference held outside the record.
     */
    public TSPTWState {
        mustVisit = (BitSet) mustVisit.clone();
    }

    @Override
    public String toString() {
        return String.format("TSPTWState(currentCity=%d, time=%d, mustVisit=%s)", currentCity, time, mustVisit);
    }
}
