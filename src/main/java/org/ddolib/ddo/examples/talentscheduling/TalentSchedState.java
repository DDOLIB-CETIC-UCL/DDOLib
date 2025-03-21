package org.ddolib.ddo.examples.talentscheduling;

import java.util.BitSet;

public record TalentSchedState(BitSet remainingScenes, BitSet maybeScenes) {

    @Override
    public String toString() {
        return String.format("Remaining: %s - Maybe: %s", remainingScenes.toString(), maybeScenes.toString());
    }
}
