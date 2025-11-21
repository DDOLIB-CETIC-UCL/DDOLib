package org.ddolib.examples.tsalt;

import java.util.BitSet;

public record TSState(BitSet remainingScenes, BitSet onLocationActors) {
    @Override
    public String toString() {
        return String.format("Remaining: %s - On location actors: %s", remainingScenes.toString(), onLocationActors.toString());
    }
}
