package org.ddolib.examples.salbp;

import java.util.*;

public record SALBPState(
        int currentBinSpace,
        BitSet remainingItems) {


    @Override
    public String toString() {
        return String.format("\nCurrent bin space : %d\nRemaining items : %s",
                currentBinSpace, remainingItems.toString());
    }
}