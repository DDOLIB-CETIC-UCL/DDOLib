package org.ddolib.examples.talentscheduling;

import java.util.BitSet;

/**
 * Represents a state in the Talent Scheduling Problem (TSP).
 *
 * <p>
 * A state keeps track of which scenes still need to be scheduled and which scenes are
 * partially scheduled in the context of merged states. It is used in decision diagrams,
 * branch-and-bound, or relaxation-based solvers for the TSP.
 * </p>
 *
 * @param remainingScenes A {@link BitSet} containing all the scenes that must still be scheduled.
 * @param maybeScenes     A {@link BitSet} used in merged states. It contains scenes that
 *                        must be scheduled in some of the merged states but have already
 *                        been scheduled in other ones.
 *
 * <p>
 * The combination of {@code remainingScenes} and {@code maybeScenes} allows the solver
 * to represent both definite and potential decisions in a relaxed or merged state.
 * </p>
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
