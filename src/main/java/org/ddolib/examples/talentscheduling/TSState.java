package org.ddolib.examples.talentscheduling;

import java.util.BitSet;

/**
 * Represents a state in the Talent Scheduling Problem (TalentSched).
 *
 * <p>
 * A state keeps track of:
 * </p>
 * <ul>
 *     <li>{@code remainingScenes} — a {@link BitSet} containing all scenes that still need to be scheduled.</li>
 *     <li>{@code maybeScenes} — a {@link BitSet} used in merged or relaxed states, containing scenes that may need to be scheduled in some merged branches but have already been scheduled in others.</li>
 *     <li>{@code onLocationActors} — a {@link BitSet} representing which actors are currently on location.</li>
 * </ul>
 *
 *
 * <p>
 * The combination of {@code remainingScenes} and {@code maybeScenes} allows the solver
 * to represent both definite and potential scheduling decisions in relaxed or merged states.
 * The {@code onLocationActors} field helps track actor availability for scene scheduling.
 * </p>
 *
 * <p>
 * This record is typically used in decision diagram-based solvers, branch-and-bound, or
 * relaxation algorithms for the TSP.
 * </p>
 *
 * @param remainingScenes  scenes that must still be scheduled
 * @param maybeScenes      scenes that may still be scheduled due to state merging
 * @param onLocationActors actors currently available on location
 */
public record TSState(BitSet remainingScenes, BitSet maybeScenes, BitSet onLocationActors) {


    @Override
    public String toString() {
            return String.format("Remaining: %s - Maybe: %s - On location actors: %s", remainingScenes.toString(), maybeScenes.toString(), onLocationActors.toString());
    }
}
