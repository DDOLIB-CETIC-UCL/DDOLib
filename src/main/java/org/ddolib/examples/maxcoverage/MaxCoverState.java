package org.ddolib.examples.maxcoverage;

import java.util.BitSet;

public record MaxCoverState (BitSet coveredItems) {

    @Override
    public String toString() {return "RemainingItems " +  this.coveredItems();}
}
