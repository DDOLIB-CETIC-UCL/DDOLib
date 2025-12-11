package org.ddolib.examples.binpacking;

import java.util.*;
import java.util.stream.Collectors;

public class BPPState {
    HashSet<Integer> remainingItems;
    int remainingTotalWeight;
    int remainingSpace;
    int usedBins;
    int wastedSpace;

    BPPState(int remainingSpace, HashSet<Integer> remainingItems, int usedBins, int remainingTotalWeight, int wastedSpace) {
        this.remainingSpace = remainingSpace;
        this.remainingItems = remainingItems;
        this.usedBins = usedBins;
        this.remainingTotalWeight = remainingTotalWeight;
        this.wastedSpace = wastedSpace;
    }

    public BPPState packItem(int item, int itemWeight) {
        HashSet<Integer> newRemainingItems = new HashSet<>(remainingItems);
        newRemainingItems.remove(item);
        return new BPPState(remainingSpace-itemWeight,newRemainingItems,usedBins,remainingTotalWeight-itemWeight, wastedSpace);
    }

    public BPPState newBin(int maxSpace) {
        return new BPPState(maxSpace, remainingItems,usedBins+1, remainingTotalWeight, wastedSpace + remainingSpace);
    }

    public boolean itemFitInBin(int itemWeight){
        return remainingSpace >= itemWeight;
    }

    @Override
    public String toString() {
        String remainingItemsAndWeight = String.join(" - ", remainingItems.stream().map(Object::toString).toList());
        return String.format("\n\tUsed bins : %d\n\tTotal wasted space : %d\n\tRemaining item to pack : %s\n\tCurrent bin space : %d\n",
                usedBins, wastedSpace, remainingItemsAndWeight, remainingSpace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remainingItems,remainingSpace,usedBins,remainingTotalWeight);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() == this.getClass()){
            BPPState other = (BPPState) obj;
            return other.usedBins == this.usedBins &&
                    other.remainingSpace == this.remainingSpace &&
                    other.remainingItems.equals(this.remainingItems) &&
                    other.remainingTotalWeight == this.remainingTotalWeight;
        }
        return false;
    }
}
