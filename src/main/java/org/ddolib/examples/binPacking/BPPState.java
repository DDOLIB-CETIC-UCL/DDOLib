package org.ddolib.examples.binPacking;

import java.util.*;

public record BPPState(
    int currentBinSpace,
    BitSet remainingItems,
    int lastRemainingSpace) {


    @Override
    public String toString() {
        return String.format("\nCurrent bin space : %d\tLast remaining space : %d\nRemaining items : %s",
                currentBinSpace, lastRemainingSpace, remainingItems.toString());
    }
}
