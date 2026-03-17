package org.ddolib.examples.binPacking;

import java.util.*;

public class BPPState {
    int currentBinSpace;
    int usedBins;
    BPPProblem problem;
    BitSet remainingItems;


    BPPState(int currentBinSpace, int usedBins, BPPProblem problem, BitSet remainingItems) {
        this.currentBinSpace = currentBinSpace;
        this.usedBins = usedBins;
        this.problem = problem;
        this.remainingItems = remainingItems;
    }

    public BPPState packItem(int item) {
        int nCurrentBinSpace;
        int nUsedBins = usedBins;
        BitSet nRemainingItems = (BitSet) remainingItems.clone();
        int itemWeight = problem.itemWeights[item];
        if(currentBinSpace - itemWeight < 0) {
            nCurrentBinSpace = problem.binMaxSpace-itemWeight;
            nUsedBins++;
        } else {
            nCurrentBinSpace = currentBinSpace - itemWeight;
        }
        nRemainingItems.set(item, false);
        return new BPPState(nCurrentBinSpace, nUsedBins, problem, nRemainingItems);
    }

    public Iterator<Integer> fittingItems() {
        int nextItem = remainingItems.nextSetBit(0);
        HashSet<Integer> allItems = new HashSet<>();
        HashSet<Integer> fittingItems = new HashSet<>();
        while (nextItem != -1) {
            if(problem.itemWeights[nextItem] == currentBinSpace) {
                return List.of(nextItem).iterator();
            } else if(problem.itemWeights[nextItem] <= currentBinSpace) {
                fittingItems.add(nextItem);
            }
            allItems.add(nextItem);
            nextItem = remainingItems.nextSetBit(nextItem+1);
        }
        if(!fittingItems.isEmpty()) return fittingItems.iterator();
        return allItems.iterator();
    }

    @Override
    public String toString() {
        return String.format("Used bins : %d\tCurrent bin space : %d\nRemaining items : %s", usedBins, currentBinSpace, remainingItems.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentBinSpace,usedBins);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == this.getClass()) {
            BPPState other = (BPPState) obj;
            return currentBinSpace == other.currentBinSpace && usedBins == other.usedBins && remainingItems.equals(other.remainingItems);
        }
        return false;
    }
}
