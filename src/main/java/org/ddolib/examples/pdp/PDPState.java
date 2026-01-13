package org.ddolib.examples.pdp;

import java.util.BitSet;
import java.util.Objects;
/**
 * Represents the state of a Pickup and Delivery Problem (PDP) during the search process.
 * <p>
 * A {@code PDPState} stores the nodes that can still be visited, all unvisited nodes,
 * the current node (or nodes in case of state fusion), and information about the
 * vehicle content (min and max) to track capacity constraints.
 * </p>
 * <p>
 * This class is used by search algorithms such as ACS, A*, and DDO to represent partial
 * solutions and manage state transitions efficiently.
 * </p>
 */
public class PDPState {
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
     * Computes the uncertainty on the vehicle content, defined as {@code maxContent - minContent}.
     *
     * @return the difference between maximum and minimum vehicle content
     */
    public int uncertaintyOnContent() {
        return maxContent - minContent;
    }
    /** Cached hash code for efficient use in hash-based collections. */
    private int hash;
    /**
     * Constructs a PDPState.
     *
     * @param current      the current node(s) of the vehicle
     * @param openToVisit  nodes that can currently be visited
     * @param allToVisit   all nodes that have not yet been visited
     * @param minContent   minimum vehicle content
     * @param maxContent   maximum vehicle content
     */
    public PDPState(BitSet current, BitSet openToVisit, BitSet allToVisit, int minContent, int maxContent) {
        this.openToVisit = openToVisit;
        this.allToVisit = allToVisit;
        this.current = current;
        this.minContent = minContent;
        this.maxContent = maxContent;
        this.hash = Objects.hash(openToVisit, allToVisit, current,minContent,maxContent);
    }

    public int hashCode() {
        return hash;
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
    /**
     * Returns a BitSet containing a single value.
     *
     * @param singletonValue the value to set in the BitSet
     * @return a BitSet with only the specified value set
     */
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
