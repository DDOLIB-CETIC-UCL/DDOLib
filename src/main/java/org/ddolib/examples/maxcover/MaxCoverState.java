package org.ddolib.examples.maxcover;

import java.util.BitSet;

public record MaxCoverState (BitSet coveredItems) {

    @Override
    public String toString() {return "RemainingItems " +  this.coveredItems();}
}
