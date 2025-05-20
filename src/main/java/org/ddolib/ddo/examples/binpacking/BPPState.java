package org.ddolib.ddo.examples.binpacking;

import java.util.*;

public class BPPState {
    HashSet<Integer> remainingItems;
    int remainingTotalWeight;

    private Bin currentBin;
    private int currentBinId = 0;
    private HashSet<Bin> usedBins = new HashSet<>();
    private final BPPProblem problem;
    private boolean verbose = false;

    BPPState(BPPProblem problem) {
        remainingItems = new HashSet<>();
        for(int i = 0; i < problem.nbItems; i++)remainingItems.add(i);
        remainingTotalWeight = Arrays.stream(problem.itemWeight).sum();
        this.problem = problem;
        this.currentBin = new Bin(problem,verbose);
    }

    BPPState(BPPProblem problem, Boolean verbose) {
        remainingItems = new HashSet<>();
        for(int i = 0; i < problem.nbItems; i++)remainingItems.add(i);
        remainingTotalWeight = Arrays.stream(problem.itemWeight).sum();
        this.problem = problem;
        this.verbose = verbose;
        this.currentBin = new Bin(problem,verbose);
    }

    BPPState(BPPState other) {
        this.usedBins = new HashSet<>();
        this.usedBins.addAll(other.usedBins);
        this.remainingItems = new HashSet<>();
        this.remainingItems.addAll(other.remainingItems);
        this.problem = other.problem;
        this.verbose = other.verbose;
        this.remainingTotalWeight = other.remainingTotalWeight;
        this.currentBin = new Bin(other.currentBin);
        this.currentBinId = other.currentBinId;
    }

    public int totalUsedBin(){
        return usedBins.size();
    }

    public int currentBinId(){
        return currentBinId;
    }

    public int remainingSpace(){
        return currentBin.remainingSpace();
    }

    public void packItem(int item, int itemWeight, int bin) {
        currentBin.packItem(item, itemWeight);
        remainingTotalWeight -= itemWeight;
        remainingItems.remove(item);
    }

    public void newBin() {
        usedBins.add(currentBin);
        currentBin = new Bin(problem, verbose);
        currentBinId++;
    }

    @Override
    public String toString() {
        String binString = String.format("%s%n", String.join("", usedBins.stream().map(Bin::toString).toList()));
        String remainingItemsAndWeight = String.join("", remainingItems.stream().map(item -> String.format("\tId %d - Weight %d%n",item,problem.itemWeight[item])).toList());
        return String.format("Remaining item to pack : \n%s%nCurrent bin : \t%sBins : \n%s", remainingItemsAndWeight, currentBin.toString(), binString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usedBins,remainingItems,currentBin);
    }
}
