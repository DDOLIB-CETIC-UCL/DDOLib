package org.ddolib.ddo.examples.binpacking;

import java.util.*;
import java.util.stream.Collectors;

public class BPPState {
    HashSet<Integer> remainingItems;
    int remainingSpace;
    HashSet<Integer> remainingSpaces;
    int usedBins;

    BPPState(int remainingSpace, HashSet<Integer> remainingItems, int usedBins) {
        this.remainingSpace = remainingSpace;
        this.remainingItems = remainingItems;
        this.usedBins = usedBins;
        remainingSpaces = new HashSet<>();
    }

    BPPState(HashSet<Integer> remainingSpaces, HashSet<Integer> remainingItems, int usedBins) {
        this.remainingItems = remainingItems;
        this.remainingSpace = -1;
        this.remainingSpaces = remainingSpaces;
        this.usedBins = usedBins;
    }

    public BPPState packItem(int item, int itemWeight) {
        HashSet<Integer> newRemainingItems = new HashSet<>(remainingItems);
        newRemainingItems.remove(item);
        if(remainingSpace == -1){
            HashSet<Integer> newRemainingSpaces =
                    remainingSpaces.stream().map(s -> s-itemWeight).collect(Collectors.toCollection(HashSet::new));
            return new BPPState(newRemainingSpaces,newRemainingItems,usedBins);
        } else {
            return new BPPState(remainingSpace-itemWeight,newRemainingItems,usedBins);
        }
    }

    public BPPState newBin(int maxSpace) {
        return new BPPState(maxSpace, remainingItems,usedBins+1);
    }

    public boolean itemFitInBin(int itemWeight){
        if(remainingSpace == -1){
            // If item fit, no space should be smaller than item weight.
            return remainingSpaces.stream().filter(s -> s < itemWeight).toList().isEmpty();
        } else {
            return remainingSpace >= itemWeight;
        }
    }

    @Override
    public String toString() {
        String remainingItemsAndWeight = String.join(" - ", remainingItems.stream().map(Object::toString).toList());
        return String.format("Remaining item to pack : \n%s%nCurrent bin space : %d\n", remainingItemsAndWeight, remainingSpace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remainingItems,remainingSpace,remainingSpaces,usedBins);
    }
}
