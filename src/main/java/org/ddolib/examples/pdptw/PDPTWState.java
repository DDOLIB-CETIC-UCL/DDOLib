package org.ddolib.examples.pdptw;

import java.util.BitSet;
import java.util.Objects;

public class PDPTWState {

    /**
     * Nodes that can currently be visited.
     * <p>
     * Includes all unvisited pickup nodes and all unvisited delivery nodes whose
     * corresponding pickup node has already been visited.
     * </p>
     */
    BitSet openToVisit;

    /**
     * All nodes that have not yet been visited, including those that cannot
     * currently be visited due to pickup-delivery constraints.
     */
    BitSet allToVisit;

    /**
     * The current node(s) of the vehicle.
     * <p>
     * Typically a singleton. In case of merged states (relaxation), it can contain multiple nodes.
     * </p>
     */
    BitSet current;
    /**
     * The minimum possible vehicle content (number of items) at this state.
     */
    int minContent;
    /**
     * The maximum possible vehicle content (number of items) at this state.
     */
    int maxContent ;

    /**
     * The current time at this state, or earlier in case of fusion
     */
    double currentTime;

    public PDPTWState(BitSet current, BitSet openToVisit, BitSet allToVisit, int minContent, int maxContent, double currentTime) {
        this.openToVisit = openToVisit;
        this.allToVisit = allToVisit;
        this.current = current;
        this.minContent = minContent;
        this.maxContent = maxContent;
        this.currentTime = currentTime;
    }

    public int hashCode() {
        return Objects.hash(openToVisit, allToVisit,
                current, minContent,maxContent, currentTime);
    }

    @Override
    public boolean equals(Object obj) {
        PDPTWState that = (PDPTWState) obj;
        if(this.minContent != that.minContent) return false;
        if(this.maxContent != that.maxContent) return false;
        if(this.currentTime != that.currentTime) return false;
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
        return "PDState(current:" + current + " currentTime:" + currentTime + " openToVisit:" + openToVisit + " closedToVisit:" + closedToVisit + ")";
    }
}
