package org.ddolib.examples.talentscheduling;

import java.util.BitSet;

/**
 * Represent a state for a talent scheduling instance
 *
 * @param remainingScenes Set containing all the remaining scenes that must planned.
 * @param maybeScenes     Used by merged states. Contains scenes that must be planned for some of
 *                        the merged states but has already been planned for other ones.
 */
public record TSState(BitSet remainingScenes, BitSet maybeScenes, BitSet onLocationActors) {

    /*@Override
    public boolean equals(Object obj) {
        return obj instanceof TSState && remainingScenes.equals(((TSState) obj).remainingScenes) && maybeScenes.equals(((TSState) obj).maybeScenes);
    }*/

    @Override
    public String toString() {
            return String.format("Remaining: %s - Maybe: %s - On location actors: %s", remainingScenes.toString(), maybeScenes.toString(), onLocationActors.toString());
    }
}
