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
        return new BPPState(binMaxSpace, 1, remainingItems);
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
        while (nextItem != -1) {
            if (itemWeights[nextItem] == state.currentBinSpace()) {
                return List.of(nextItem).iterator();
            } else if (itemWeights[nextItem] <= state.currentBinSpace()) {
                fittingItems.add(nextItem);
            }
            allItems.add(nextItem);
            nextItem = state.remainingItems().nextSetBit(nextItem + 1);
        }
        if (!fittingItems.isEmpty()) return fittingItems.iterator();
        return allItems.iterator();
    }

    @Override
    public BPPState transition(BPPState state, Decision decision) {
        int item = decision.value();
        int nCurrentBinSpace;
        int nUsedBins = state.usedBins();
        BitSet nRemainingItems = (BitSet) state.remainingItems().clone();
        int itemWeight = itemWeights[item];
        if (state.currentBinSpace() - itemWeight < 0) {
            nCurrentBinSpace = binMaxSpace - itemWeight;
            nUsedBins++;
        } else {
            nCurrentBinSpace = state.currentBinSpace() - itemWeight;
        }
        nRemainingItems.set(item, false);
        return new BPPState(nCurrentBinSpace, nUsedBins, nRemainingItems);
    }

    @Override
    public double transitionCost(BPPState state, Decision decision) {
        int item = decision.value();
        if (state.currentBinSpace() < itemWeights[item]) return 1;
        else return 0;
    }
}


