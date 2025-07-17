package org.ddolib.example.ddo.tsptw;

import java.util.BitSet;

public record TSPTWDominanceKey(Position p, BitSet mustVisit) {
    @Override
    public String toString() {
        return String.format("position: %s - must visit: %s",
                p,
                mustVisit);
    }
}
