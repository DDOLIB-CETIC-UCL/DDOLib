package org.ddolib.examples.tsptw;

import java.util.BitSet;
/**
 * Key used for dominance checking in the Traveling Salesman Problem with Time Windows (TSPTW).
 *
 * <p>
 * A {@link TSPTWDominanceKey} uniquely identifies a group of states that share the same
 * current position {@code p} and the same set of locations that still must be visited
 * ({@code mustVisit}). It is used by {@link TSPTWDominance} to determine which states
 * can be compared for dominance.
 * </p>
 *
 * <p>
 * Two states with the same dominance key are comparable: the state with the lower
 * current time dominates the other, allowing pruning in the search.
 * </p>
 *
 * @param p The current position in the tour.
 * @param mustVisit The set of locations that must still be visited.
 */
public record TSPTWDominanceKey(Position p, BitSet mustVisit) {
    @Override
    public String toString() {
        return String.format("position: %s - must visit: %s",
                p,
                mustVisit);
    }
}
