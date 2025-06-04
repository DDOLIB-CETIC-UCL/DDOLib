package org.ddolib.ddo.examples.routing.cvrp;

import org.ddolib.ddo.examples.routing.util.RoutePosition;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

public record CVRPState(RoutePosition[] pos, int[] capacity, int lastUsedVehicle, BitSet mustVisit, BitSet maybeVisit,
                        int depth) {
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CVRPState other) {
            return Arrays.equals(this.pos, other.pos)
                    && Arrays.equals(this.capacity, other.capacity)
                    && this.mustVisit == other.mustVisit
                    && this.maybeVisit == other.maybeVisit
                    && this.depth == other.depth;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(pos), Arrays.hashCode(capacity), mustVisit, maybeVisit, depth);
    }

    @Override
    public String toString() {
        return String.format(
                "positions: %s - capacities: %s - must visit: %s - maybe visit: %s - depth: %d",
                Arrays.toString(pos),
                Arrays.toString(capacity),
                mustVisit,
                maybeVisit,
                depth
        );
    }
}
