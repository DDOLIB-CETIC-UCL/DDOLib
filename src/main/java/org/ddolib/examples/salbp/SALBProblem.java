package org.ddolib.examples.salbp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.util.*;

public class SALBProblem implements Problem<SALBPState> {

    int nbItems;
    int binMaxSpace;
    int[] itemWeights;
    BitSet[] allPrecedences;
    // Optimal solution
    Optional<Double> optimal;
    Optional<String> name;

    SALBProblem(int nbItems, int binMaxSpace, int[] itemWeights, BitSet[] precedences, Optional<Double> optimal) {
        this.nbItems = nbItems;
        this.binMaxSpace = binMaxSpace;
        this.itemWeights = itemWeights;
        this.allPrecedences = precedences;
        this.optimal = optimal;
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public double evaluate(int[] solution) throws InvalidSolutionException {
        BitSet remainingItems = new BitSet(nbItems);
        remainingItems.set(0, nbItems);
        int currentBinSpace = binMaxSpace;
        int bins = 1;
        for (int item : solution) {
            BitSet precedence = (BitSet) allPrecedences[item].clone();
            precedence.and(remainingItems);
            if (!precedence.isEmpty())
                throw new InvalidSolutionException(String.format(
                        "Trying to insert %d but precedences %s are not inserted yet", item, precedence));
            int weight = itemWeights[item];
            if (currentBinSpace < weight) {
                currentBinSpace = binMaxSpace - weight;
                bins++;
            } else {
                currentBinSpace -= weight;
            }
            remainingItems.clear(item);
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
    public SALBPState initialState() {
        BitSet remainingItems = new BitSet(nbItems);
        remainingItems.set(0, nbItems);
        return new SALBPState(binMaxSpace, remainingItems);
    }

    @Override
    public double initialValue() {
        // Starting with one opened bin.
        return 1;
    }

    @Override
    public Iterator<Integer> domain(SALBPState state, int var) {
        if (var >= nbVars()) return Collections.emptyIterator();

        int nextItem = state.remainingItems().nextSetBit(0);
        HashSet<Integer> allItems = new HashSet<>();
        HashSet<Integer> fittingItems = new HashSet<>();

        while (nextItem != -1) {
            BitSet precedence = (BitSet) allPrecedences[nextItem];
            precedence.and(state.remainingItems());
            if (precedence.isEmpty()){
                if (itemWeights[nextItem] == state.currentBinSpace()) {
                    return List.of(nextItem).iterator();
                } else if (itemWeights[nextItem] < state.currentBinSpace()) {
                    fittingItems.add(nextItem);
                }
                allItems.add(nextItem);
            }
            nextItem = state.remainingItems().nextSetBit(nextItem + 1);
        }

        if (!fittingItems.isEmpty()) {
            return fittingItems.iterator();
        }
        return allItems.iterator();
    }

    @Override
    public SALBPState transition(SALBPState state, Decision decision) {
        int item = decision.value();
        int itemWeight = itemWeights[item];
        boolean binFull = state.currentBinSpace() - itemWeight < 0;

        int currentBinSpace = binFull ? binMaxSpace - itemWeight : state.currentBinSpace() - itemWeight;
        BitSet remainingItems = (BitSet) state.remainingItems().clone();
        remainingItems.set(item, false);

        return new SALBPState(currentBinSpace, remainingItems);
    }

    @Override
    public double transitionCost(SALBPState state, Decision decision) {
        int item = decision.value();
        if (state.currentBinSpace() < itemWeights[item]) return 1;
        else return 0;
    }

    @Override
    public String toString() {
        return name.orElse("No name");
    }
}

