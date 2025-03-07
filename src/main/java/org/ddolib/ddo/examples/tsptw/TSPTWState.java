package org.ddolib.ddo.examples.tsptw;

import java.util.BitSet;

public record TSPTWState(Position position, int time, BitSet mustVisit, BitSet possiblyVisit, int depth) {

    @Override
    public String toString() {
        return String.format("position: %s - time: %d - must visit: %s - possibly visit: %s - depth: %d",
                position,
                time,
                mustVisit,
                possiblyVisit,
                depth);
    }
}
