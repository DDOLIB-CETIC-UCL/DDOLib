package org.ddolib.examples.nolayer.tsp;

import java.util.BitSet;
import java.util.Objects;

/**
 * Represents a state in the Traveling Salesman Problem (TSP) for nolayer models.
 */
public class TSPState {
    /**
     * Set of nodes that have not been visited yet.
     */
    public BitSet toVisit;

    /**
     * Current node(s). Usually a singleton, but can be multiple in merged states.
     */
    public BitSet current;

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
        if (this == obj) return true;
        if (!(obj instanceof TSPState)) return false;
        TSPState that = (TSPState) obj;
        return (this.current.equals(that.current)) && this.toVisit.equals(that.toVisit);
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
