package org.ddolib.examples.binpacking;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.InvalidSolutionException;
import org.ddolib.modeling.Problem;

import java.io.*;
import java.util.*;

/**
 * Represents an instance of the Bin Packing Problem.
 *
 * <p>
 * The Bin Packing problem consists in filling items of different sizes in bins of same fixed size.
 * The goal is to minimize the number of used bins.
 * </p>
 *
 * <p>
 * Each iteration we select the next item to add in the current/new bin.
 * The state of the problem is :
 * </p>
 * <ul>
 *     <li>The current bin space</li>
 *     <li>The remaining items</li>
 *     <li>The last remaining space. Mainly used to order the bin from fullest to emptiest and break symmetries.</li>
 * </ul>
 */
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

    BPPProblem(String fileName) throws FileNotFoundException {
        final File f = new File(fileName);
        int nbItems = 0;
        int binMaxSpace = 0;
        Integer[] itemWeights = new Integer[1];
        int lineCounter = 0;
        Optional<Double> optimal = Optional.empty();
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = bf.readLine()) != null) {
                if (lineCounter == 0) {
                    String[] splitLine = line.split("\\s+");
                    if (splitLine.length > 1) {
                        optimal = Optional.of(Double.parseDouble(splitLine[1]));
                    }
                    nbItems = Integer.parseInt(splitLine[0]);
                    itemWeights = new Integer[nbItems];
                } else if (lineCounter == 1) {
                    binMaxSpace = Integer.parseInt(line);
                } else {
                    itemWeights[lineCounter - 2] = Integer.parseInt(line);
                }
                lineCounter++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Arrays.sort(itemWeights, Comparator.reverseOrder());

        this.nbItems = nbItems;
        this.binMaxSpace = binMaxSpace;
        this.itemWeights = Arrays.stream(itemWeights).mapToInt(i -> i).toArray();
        this.optimal = optimal;
        this.name = Optional.of(fileName);
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
        ArrayList<Integer> allItems = new ArrayList<>();
        ArrayList<Integer> fittingItems = new ArrayList<>();

        // Trying to ensure an increasing remaining space bin order.
        while (nextItem != -1) {
            int weight = itemWeights[nextItem];
            int currentAllowedSpace = state.currentBinSpace() - state.lastRemainingSpace();

            // If we find a perfectly fitting item and last closed bin was full or do not exist. Take this item.
            if (weight == currentAllowedSpace) {
                return List.of(nextItem).iterator();
                // If an item fits and resulting bin have more space than last closed bin, we can use it.
            } else if (weight < currentAllowedSpace) {
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
        int itemWeight = itemWeights[item];
        boolean binFull = state.currentBinSpace() - itemWeight < 0;

        int lastRemainingSpace = binFull ? state.currentBinSpace() : state.lastRemainingSpace();
        int currentBinSpace = binFull ? binMaxSpace - itemWeight : state.currentBinSpace() - itemWeight;
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


