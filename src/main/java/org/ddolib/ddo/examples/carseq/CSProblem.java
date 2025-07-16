package org.ddolib.ddo.examples.carseq;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CSProblem implements Problem<CSState> {
    public final int nCars; // Number of cars
    public final int[] classSize; // Number of cars in each class
    public final int[] blockSize; // Size of the block for each option
    public final int[] blockMax; // For each option, max number of cars with the option in its block
    public final boolean[][] carOptions; // For each option and each car class, true if the car has the option

    public CSProblem(int[] classSize, int[] blockSize, int[] blockMax, boolean[][] carOptions) {
        int nCars = 0;
        for (int size : classSize) nCars += size;
        this.nCars = nCars;
        this.classSize = Arrays.copyOf(classSize, classSize.length + 1); // Add joker car class for relax
        this.blockSize = blockSize;
        this.blockMax = blockMax;
        this.carOptions = Arrays.copyOf(carOptions, carOptions.length + 1); // Add joker car class for relax
        this.carOptions[carOptions.length] = new boolean[blockSize.length];
    }

    /**
     * @brief Number of classes of cars
     */
    public int nClasses() {
        return carOptions.length - 1;
    }

    /**
     * @brief Number of options
     */
    public int nOptions() {
        return blockSize.length;
    }

    @Override
    public int nbVars() {
        return nCars;
    }

    @Override
    public CSState initialState() {
        return new CSState(classSize, new long[nOptions()]);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(CSState state, int var) {
        ArrayList<Integer> next = new ArrayList<>();
        for (int i = 0; i < nClasses() + 1; i++) {
            if (state.carsToBuild()[i] > 0) { // Build car from class i for next state
                next.add(i);
            }
        }
        return next.iterator();
    }

    @Override
    public CSState transition(CSState state, Decision decision) {
        int[] nextCarsToBuild = Arrays.copyOf(state.carsToBuild(), nClasses() + 1);
        nextCarsToBuild[decision.val()]--; // Built a car in class [decision.val()]
        long[] nextPreviousBlocks = new long[nOptions()];
        for (int i = 0; i < nOptions(); i++) { // Shift blocks and add new car to them
            nextPreviousBlocks[i] = (state.previousBlocks()[i] << 1) & ((1L << blockSize[i]) - 1) | (carOptions[decision.val()][i] ? 1 : 0);
        }
        return new CSState(nextCarsToBuild, nextPreviousBlocks);
    }

    @Override
    public double transitionCost(CSState state, Decision decision) {
        double cost = 0;
        for (int i = 0; i < nOptions(); i++) { // Shift blocks and add new car to them
            long nextBlock = (state.previousBlocks()[i] << 1) & ((1L << blockSize[i]) - 1) | (carOptions[decision.val()][i] ? 1 : 0);
            int additional = Long.bitCount(nextBlock) - blockMax[i];
            if (additional > 0) cost -= additional; // Too many cars with that option recently
        }
        return cost;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(String.format("CSProblem [\n\tnClasses = %d, nCars = %d, nOptions = %d\n", nClasses(), nCars, nOptions()));
        s.append("\tCars");
        for (int i = 0; i < nClasses(); i++) {
            s.append(" " + classSize[i]);
        }
        s.append("\n");
        for (int i = 0; i < nOptions(); i++) {
            s.append(String.format("\tOption %d: %d/%d\n", i, blockMax[i], blockSize[i]));
        }
        for (int i = 0; i < nClasses(); i++) {
            s.append(String.format("\tClass %d: options", i));
            for (int j = 0; j < nOptions(); j++) {
                if (carOptions[i][j]) s.append(" " + j);
            }
            s.append("\n");
        }
        s.append("]");
        return s.toString();
    }

    public String solutionToString(int[] cars, int bestValue) {
        StringBuilder s = new StringBuilder(String.format(
            "CSSolution [\n\tscore = %d, cars : %s\n", -bestValue,
            Arrays.stream(cars).mapToObj(String::valueOf).collect(Collectors.joining(" "))
        ));
        for (int classIndex : cars) {
            s.append(String.format("\tclass %d : ", classIndex));
            for (int i = 0; i < nOptions(); i++) {
                s.append(carOptions[classIndex][i] ? " 1" : " 0");
            }
            s.append("\n");
        }
        s.append("]");
        return s.toString();
    }
}