package org.ddolib.examples.binPacking2;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

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
                currentBinSpace = binMaxSpace-weight;
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
        return new BPPState(binMaxSpace, 1, this, remainingItems);
    }

    @Override
    public double initialValue() {
        // Starting with one opened bin.
        return 1;
    }

    @Override
    public Iterator<Integer> domain(BPPState state, int var) {
        if (var >= nbVars()) return Collections.emptyIterator();

        return state.fittingItems();
    }

    @Override
    public BPPState transition(BPPState state, Decision decision) {
        int item = decision.value();
        return state.packItem(item);
    }

    @Override
    public double transitionCost(BPPState state, Decision decision) {
        int item = decision.value();
        if (state.currentBinSpace < itemWeights[item]) return 1;
        else return 0;
    }
}


