package org.ddolib.nolayer.examples.tsptw;

import java.util.BitSet;

/**
 * State for the NoLayer formulation of the Traveling Salesman Problem with Time Windows.
 *
 * @param currentCity The index of the current city the vehicle is located at.
 * @param time        The current arrival time at the current city.
 * @param mustVisit   The set of remaining cities that must be visited.
 */
public record TSPTWState(int currentCity, int time, BitSet mustVisit) {
    public TSPTWState {
        mustVisit = (BitSet) mustVisit.clone();
    }

    @Override
    public String toString() {
        return String.format("TSPTWState(currentCity=%d, time=%d, mustVisit=%s)", currentCity, time, mustVisit);
    }
}
