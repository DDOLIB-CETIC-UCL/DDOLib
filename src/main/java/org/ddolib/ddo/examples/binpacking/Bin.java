package org.ddolib.ddo.examples.binpacking;

import java.util.HashSet;
import java.util.Objects;

public class Bin {
    private int totalWeight = 0;
    private HashSet<Integer> items = new HashSet<>();
    private final BPPProblem problem;
    private final boolean verbose;

    public Bin(BPPProblem problem, boolean verbose) {
        this.problem = problem;
        this.verbose = verbose;
    }

    public Bin(Bin other) {
        this.problem = other.problem;
        this.totalWeight = other.totalWeight;
        this.verbose = other.verbose;
        if (verbose) {
            this.items = new HashSet<>();
            items.addAll(other.items);
        }
    }

    public void packItem(int item, int weight) {
        if (verbose) items.add(item);
        totalWeight += weight;
    }

    public int remainingSpace() {
        return problem.binMaxSpace - totalWeight;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder(String.format("Total weight : %d - Remaining weight : %d%n", totalWeight, problem.binMaxSpace - totalWeight));
        for (int item : items) {
            res.append(String.format("Item %d - weight %d%n", item, problem.itemWeight[item]));
        }
        return res.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalWeight, items);
    }
}
