package org.ddolib.examples.tspnolayer;

import java.util.BitSet;
import java.util.Objects;

/**
 * Represents a state in the Traveling Salesman Problem (TSP) for nolayer models.
 */
public class TSPNoLayerState {
    /**
     * Set of nodes that have not been visited yet.
     */
    public BitSet toVisit;

    /**
     * Current node(s). Usually a singleton, but can be multiple in merged states.
     */
    public BitSet current;

    /**
     * Constructs a new TSPNoLayerState.
     *
     * @param current the current node(s)
     * @param toVisit the nodes that have not yet been visited
     */
    public TSPNoLayerState(BitSet current, BitSet toVisit) {
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
        if (!(obj instanceof TSPNoLayerState)) return false;
        TSPNoLayerState that = (TSPNoLayerState) obj;
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
            return "TSPNoLayerState(possibleCurrent:" + current + " toVisit:" + toVisit + ")";
        } else {
            return "TSPNoLayerState(current:" + current.nextSetBit(0) + " toVisit:" + toVisit + ")";
        }
    }
}
