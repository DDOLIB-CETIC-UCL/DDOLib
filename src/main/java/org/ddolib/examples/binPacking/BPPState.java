package org.ddolib.examples.binPacking;

import java.util.*;

public record BPPState(
    int currentBinSpace,
    int usedBins,
    BitSet remainingItems) {


    @Override
    public String toString() {
        return String.format("Used bins : %d\tCurrent bin space : %d\nRemaining items : %s", usedBins, currentBinSpace, remainingItems.toString());
    }
}
