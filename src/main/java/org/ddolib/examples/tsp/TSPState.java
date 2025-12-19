package org.ddolib.examples.tsp;

import java.util.BitSet;
import java.util.Objects;
/**
 * Represents a state in the Traveling Salesman Problem (TSP).
 *
 * <p>
 * A {@code TSPState} captures the current situation of the tour:
 * </p>
 * <ul>
 *     <li>{@code current} – the set of nodes currently being considered as the current location.
 *         In most cases, this is a singleton, but during state merging (relaxation), it may contain multiple nodes.</li>
 *     <li>{@code toVisit} – the set of nodes that have not yet been visited.</li>
 * </ul>
 *
 * <p>
 * This class provides methods to handle singleton nodes, and overrides {@code equals}, {@code hashCode},
 * and {@code toString} for proper use in collections and debugging.
 * </p>
 *
 * @see TSPProblem
 * @see TSPRelax
 */
public class TSPState {
    /**
     * Set of nodes that have not been visited yet.
     */
    BitSet toVisit;

    /**
     * Current node(s). Usually a singleton, but can be multiple in merged states.
     */
    BitSet current;
    /**
     * Constructs a new TSPState.
     *
     * @param current the current node(s)
     * @param toVisit the nodes that have not yet been visited
     */
    public TSPState(BitSet current, BitSet toVisit) {
        this.toVisit = toVisit;
        this.current = current;
    }

    @Override
    public int hashCode() {
        return Objects.hash(toVisit, current);
    }

    @Override
    public boolean equals(Object obj) {
        TSPState that = (TSPState) obj;
        return (this.current.equals(that.current))
                && this.toVisit.equals(that.toVisit);
    }
    /**
     * Creates a BitSet containing only the specified singleton value.
     *
     * @param singletonValue the value to set
     * @return a BitSet with a single bit set
     */
    public BitSet singleton(int singletonValue) {
        BitSet toReturn = new BitSet(singletonValue + 1);
        toReturn.set(singletonValue);
        return toReturn;
    }

    @Override
    public String toString() {
        if (current.cardinality() != 1) {
            return "TSPState(possibleCurrent:" + current + " toVisit:" + toVisit + ")";
        } else {
            return "TSPState(current:" + current.nextSetBit(0) + " toVisit:" + toVisit + ")";
        }
    }
}