package org.ddolib.ddo.examples.binpacking;

import java.util.HashSet;
import java.util.List;

public class Bin {
    int totalWeight = 0;
    HashSet<Integer> items = new HashSet<>();
    final BPPProblem problem;

    public Bin(BPPProblem problem) {
        this.problem = problem;
    }

    public Bin(Bin other){
        this.problem = other.problem;
        this.totalWeight = other.totalWeight;
        this.items = new HashSet<>();
        items.addAll(other.items);
    }

    public void packItem(int item, int weight){
        items.add(item);
        totalWeight += weight;
    }

    public int remainingSpace(){
        return problem.binMaxSpace - totalWeight;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder(String.format("Total weight : %d - Remaining weight : %d%n", totalWeight, problem.binMaxSpace-totalWeight));
        List<Integer> sortedItems = items.stream().sorted().toList();
        for(int item : items){
            res.append(String.format("Item %d - weight %d%n", item, problem.itemWeight[item]));
        }
        return res.toString();
    }

    public void copy(Bin bin) {
        this.totalWeight = bin.totalWeight;
        this.items = new HashSet<>();
        items.addAll(bin.items);
    }
}
