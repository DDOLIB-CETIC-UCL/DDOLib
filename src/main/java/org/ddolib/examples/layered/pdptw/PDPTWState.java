package org.ddolib.examples.layered.pdptw;

import java.util.BitSet;
import java.util.Objects;

/**
 * State representation for the Pickup and Delivery Problem with Time Windows (PDPTW).
 */
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
    int maxContent;

    /**
     * The current time at this state, or earlier in case of fusion
     */
    double minCurrentTime;
    double maxCurrentTime;

    /**
     * Builds a PDPTW state with interval-valued content/time information.
     *
     * @param current        current node(s)
     * @param openToVisit    currently reachable unvisited nodes
     * @param allToVisit     all remaining unvisited nodes
     * @param minContent     lower bound on vehicle load
     * @param maxContent     upper bound on vehicle load
     * @param minCurrentTime lower bound on current time
     * @param maxCurrentTime upper bound on current time
     */
    public PDPTWState(BitSet current, BitSet openToVisit, BitSet allToVisit, int minContent, int maxContent, double minCurrentTime, double maxCurrentTime) {
        this.openToVisit = openToVisit;
        this.allToVisit = allToVisit;
        this.current = current;
        this.minContent = minContent;
        this.maxContent = maxContent;
        this.minCurrentTime = minCurrentTime;
        this.maxCurrentTime = maxCurrentTime;
    }

    public int hashCode() {
        return Objects.hash(openToVisit, allToVisit,
                current, minContent, maxContent, minCurrentTime, maxCurrentTime);
    }

    @Override
    public boolean equals(Object obj) {
        PDPTWState that = (PDPTWState) obj;
        if (this.minContent != that.minContent) return false;
        if (this.maxContent != that.maxContent) return false;
        if (this.minCurrentTime != that.minCurrentTime) return false;
        if (this.maxCurrentTime != that.maxCurrentTime) return false;
        if (!that.current.equals(this.current)) return false;
        if (!that.openToVisit.equals(this.openToVisit)) return false;
        return (that.allToVisit.equals(this.allToVisit));
    }

    /**
     * Builds a singleton {@link BitSet} containing only the given value.
     *
     * @param singletonValue the value to set in the returned set
     * @return a new {@link BitSet} with only {@code singletonValue} set
     */
    public BitSet singleton(int singletonValue) {
        BitSet toReturn = new BitSet(singletonValue + 1);
        toReturn.set(singletonValue);
        return toReturn;
    }

    /**
     * Formats a possibly imprecise value as either a single number, when {@code min}
     * and {@code max} are equal, or as an interval {@code [min;max]} otherwise.
     *
     * @param min the lower bound of the value
     * @param max the upper bound of the value
     * @return the formatted value
     */
    public String printInterval(double min, double max) {
        if (min == max) return "" + min;
        else return "[" + min + ";" + max + "]";
    }

    /**
     * Formats a possibly imprecise value as either a single number, when {@code min}
     * and {@code max} are equal, or as an interval {@code [min;max]} otherwise.
     *
     * @param min the lower bound of the value
     * @param max the upper bound of the value
     * @return the formatted value
     */
    public String printInterval(int min, int max) {
        if (min == max) return "" + min;
        else return "[" + min + ";" + max + "]";
    }

    @Override
    public String toString() {
        BitSet closedToVisit = (BitSet) allToVisit.clone();
        closedToVisit.xor(openToVisit);
        return "PDState(current:" + current + " currentTime:" + printInterval(minCurrentTime, maxCurrentTime)
                + " openToVisit:" + openToVisit + " closedToVisit:" + closedToVisit + " allToVisit:" + allToVisit
                + " content:" + printInterval(minContent, maxContent) + ")";
    }
}
