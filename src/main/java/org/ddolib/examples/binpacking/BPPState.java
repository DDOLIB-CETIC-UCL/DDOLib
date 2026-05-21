package org.ddolib.examples.binpacking;

import java.util.*;

/**
 * The state of a Bin Packing Problem.
 * <p>
 * We fill a bin at a time. Each iteration we select the next item to place in the current bin.
 *
 * @param currentBinSpace    The remaining space of the current bin.
 * @param remainingItems     The remaining items to place in the bins.
 * @param lastRemainingSpace The remaining space of the last CLOSED bin.
 *                           Used to sort the bin from fullest to emptiest and avoid duplicated states.
 */
public record BPPState(
        int currentBinSpace,
        BitSet remainingItems,
        int lastRemainingSpace) {


    @Override
    public String toString() {
        return String.format("\nCurrent bin space : %d\tLast remaining space : %d\nRemaining items : %s",
                currentBinSpace, lastRemainingSpace, remainingItems.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BPPState(int binSpace, BitSet items, int remainingSpace)) {
            return currentBinSpace == binSpace
                    && lastRemainingSpace == remainingSpace
                    && remainingItems.equals(items);
        }
        return false;
    }
}
