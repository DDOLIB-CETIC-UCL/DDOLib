package org.ddolib.examples.ddo.tsp;

import java.util.BitSet;
import java.util.Objects;

public class TSPState {

    //every node that has not been visited yet
    BitSet toVisit;

    //the current node. It is a set because in case of a fusion, we must take the union.
    // However, most of the time, it is a singleton
    BitSet current;

    public TSPState(BitSet current, BitSet toVisit) {
        this.toVisit = toVisit;
        this.current = current;
    }

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

    @Override
    public boolean equals(Object o) {
        if (o instanceof TSPState oState) {
            return toVisit.equals(oState.toVisit) && current.equals(oState.current);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(toVisit, current);
    }
}