package org.ddolib.ddo.examples.TSPTW;

import java.util.BitSet;

public record TSPTWState(Position position, int time, BitSet mustVisit, BitSet possiblyVisit, int depth) {

    @Override
    public String toString() {
        return String.format("position: %s - time: %d - must visit: %s - might visit: %s - depth: %d",
                position,
                time,
                mustVisit,
                possiblyVisit,
                depth);
    }
}
