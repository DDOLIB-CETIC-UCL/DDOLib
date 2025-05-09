package org.ddolib.ddo.examples.binpacking;

import java.util.*;

public class BPPState {
    ArrayList<Bin> bins = new ArrayList<>();
    // Set of remaining object that need to be packed sorted by its id.
    // With the hypothesis that for i,j in ids, if i < j ==> weight(i) < weight(j)
    int remainingItems;
    BPPProblem problem;

    BPPState(BPPProblem problem) {
        remainingItems = problem.nbItems;
        this.problem = problem;
    }

    BPPState(BPPState other) {
        for(Bin bin : other.bins) this.bins.add(new Bin(bin));
        this.remainingItems = other.remainingItems;
        this.problem = other.problem;
    }

    public void packItem(int item, int itemWeight, int bin) {
        bins.get(bin).packItem(item, itemWeight);
        remainingItems -= 1;
    }

    public void newBin() {
        bins.add(new Bin(problem));
    }

    @Override
    public String toString() {
        String binString = String.format("Bins : %s%n", String.join("", bins.stream().map(Bin::toString).toList()));
        return String.format("Remaining item to pack : \t%d%nBins : \n%s", remainingItems, binString);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + bins.hashCode();
        hash = 29 * hash + remainingItems;
        return hash;
    }
}
