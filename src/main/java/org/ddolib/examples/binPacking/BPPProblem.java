package org.ddolib.examples.binPacking;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.util.*;

public class BPPProblem implements Problem<BPPState> {

    int nbItems;
    int binMaxSpace;
    int[] itemWeights;
    // Optimal solution
    Optional<Double> optimal;
    Optional<String> name;

    BPPProblem(int nbItems, int binMaxSpace, int[] itemWeights, Optional<Double> optimal) {
        this.nbItems = nbItems;
        this.binMaxSpace = binMaxSpace;
        this.itemWeights = itemWeights;
        this.optimal = optimal;
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        int currentBinSpace = binMaxSpace;
        int bins = 1;
        for (int item : solution) {
            int weight = itemWeights[item];
            if (currentBinSpace < weight) {
                currentBinSpace = binMaxSpace - weight;
                bins++;
            } else {
                currentBinSpace -= weight;
            }
        }
        return bins;
    }

    public void setName(String name) {
        this.name = Optional.of(name);
    }

    @Override
    public int nbVars() {
        return nbItems;
    }

    @Override
    public BPPState initialState() {
        BitSet remainingItems = new BitSet(nbItems);
        remainingItems.set(0, nbItems);
        return new BPPState(binMaxSpace, remainingItems, 0);
    }

    @Override
    public double initialValue() {
        // Starting with one opened bin.
        return 1;
    }

    @Override
    public Iterator<Integer> domain(BPPState state, int var) {
        if (var >= nbVars()) return Collections.emptyIterator();

        int nextItem = state.remainingItems().nextSetBit(0);
        HashSet<Integer> allItems = new HashSet<>();
        HashSet<Integer> fittingItems = new HashSet<>();

        // Trying to ensure an increasing remaining space bin order.
        while (nextItem != -1) {
            // If we find a perfectly fitting item and last closed bin was full or do not exist. Take this item.
            if (itemWeights[nextItem] == state.currentBinSpace() && state.lastRemainingSpace() <= 0) {
                return List.of(nextItem).iterator();
            // If an item fits and resulting bin have more space than last closed bin, we can use it.
            } else if (itemWeights[nextItem] < state.currentBinSpace() - state.lastRemainingSpace()) {
                fittingItems.add(nextItem);
            }
            allItems.add(nextItem);
            nextItem = state.remainingItems().nextSetBit(nextItem + 1);
        }

        if (!fittingItems.isEmpty()) {
            return fittingItems.iterator();
        // If no item fits and the currentBinSpace is less than the previous closed bin. Done
        } else if (state.currentBinSpace() < state.lastRemainingSpace()) {
            return Collections.emptyIterator();
        }
        return allItems.iterator();
    }

    @Override
    public BPPState transition(BPPState state, Decision decision) {
        int item = decision.value();
        int itemWeight = itemWeights[item];
        boolean binFull = state.currentBinSpace() - itemWeight < 0;

        int lastRemainingSpace = binFull ? state.currentBinSpace() : state.lastRemainingSpace();
        int currentBinSpace = binFull ? binMaxSpace-itemWeight:state.currentBinSpace()-itemWeight;
        BitSet remainingItems = (BitSet) state.remainingItems().clone();
        remainingItems.set(item, false);

        return new BPPState(currentBinSpace, remainingItems, lastRemainingSpace);
    }

    @Override
    public double transitionCost(BPPState state, Decision decision) {
        int item = decision.value();
        if (state.currentBinSpace() < itemWeights[item]) return 1;
        else return 0;
    }

    @Override
    public String toString() {
        return name.orElse("No name");
    }
}


