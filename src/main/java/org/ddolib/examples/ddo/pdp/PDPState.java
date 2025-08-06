package org.ddolib.examples.ddo.pdp;

import java.util.BitSet;
import java.util.Objects;

public class PDPState {

    //the nodes that we can visit, including
    // all non-visited pick-up nodes
    // all non-visited  delivery nodes such that the related pick-up has been reached
    BitSet openToVisit;

    //every node that has not been visited yet
    BitSet allToVisit;

    //the current node. It is a set because in case of a fusion, we must take the union.
    // However, most of the time, it is a singleton
    BitSet current;

    int minContent;
    int maxContent ;

    public PDPState(BitSet current, BitSet openToVisit, BitSet allToVisit, int minContent, int maxContent) {
        this.openToVisit = openToVisit;
        this.allToVisit = allToVisit;
        this.current = current;
        this.minContent = minContent;
        this.maxContent = maxContent;
    }

    public int hashCode() {
        return Objects.hash(openToVisit, allToVisit, current);
    }

    @Override
    public boolean equals(Object obj) {
        PDPState that = (PDPState) obj;
        if(this.minContent != that.minContent) return false;
        if(this.maxContent != that.maxContent) return false;
        if (!that.current.equals(this.current)) return false;
        if (!that.openToVisit.equals(this.openToVisit)) return false;
        return (that.allToVisit.equals(this.allToVisit));
    }

    public BitSet singleton(int singletonValue) {
        BitSet toReturn = new BitSet(singletonValue + 1);
        toReturn.set(singletonValue);
        return toReturn;
    }

    @Override
    public String toString() {
        BitSet closedToVisit = (BitSet) allToVisit.clone();
        closedToVisit.xor(openToVisit);
        if (current.cardinality() != 1) {
            return "PDState(possibleCurrent:" + current + " openToVisit:" + openToVisit + " closedToVisit:" + closedToVisit + ")";
        } else {
            return "PDState(current:" + current.nextSetBit(0) + " openToVisit:" + openToVisit + " closedToVisit:" + closedToVisit + ")";
        }
    }
}
