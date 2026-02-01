package org.ddolib.examples.setcover;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public record SetCoverState(BitSet uncoveredItems) {
    /**
     * Returns a string representation of the state.
     *
     * @return a string describing the covered items
     */
    @Override
    public String toString() {return "uncovered items "+this.uncoveredItems();}
}
