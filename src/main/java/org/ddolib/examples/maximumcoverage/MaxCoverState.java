package org.ddolib.examples.maximumcoverage;

import java.util.BitSet;
/**
 * Represents a state in the Maximum Coverage (MaxCover) problem.
 *
 * <p>
 * Each state tracks which items have been covered so far using a {@link BitSet}.
 * This is used in Decision Diagram Optimization (DDO) and other search or
 * combinatorial algorithms to evaluate partial solutions.
 *
 * <p>
 * The record is immutable: the set of covered items cannot be changed once
 * the state is created. Use state transitions in {@link MaxCoverProblem} to
 * generate new states with additional items covered.
 *
 * @param coveredItems a BitSet representing the items that are covered in this state
 */
public record MaxCoverState (BitSet coveredItems) {
    /**
     * Returns a string representation of the state.
     *
     * @return a string describing the covered items
     */
    @Override
    public String toString() {return "covered items "+this.coveredItems();}
}
