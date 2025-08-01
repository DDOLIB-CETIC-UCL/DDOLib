package org.ddolib.examples.ddo.carseq;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


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
     * Number of car classes
     */
    public int nClasses() {
        return carOptions.length - 1;
    }

    /**
     * Number of options
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
        // Compute nWithOption
        int[] nWithOption = new int[nOptions()];
        for (int classIndex = 0; classIndex < nClasses(); classIndex++) {
            int nCars = classSize[classIndex];
            for (int optionIndex = 0; optionIndex < nOptions(); optionIndex++) {
                if (carOptions[classIndex][optionIndex]) {
                    nWithOption[optionIndex] += nCars;
                }
            }
        }
        return new CSState(this, classSize, new long[nOptions()], nWithOption, nCars);
    }


    @Override
    public double initialValue() {
        return 0;
    }


    @Override
    public Iterator<Integer> domain(CSState state, int var) {
        return IntStream.range(0, nClasses() + 1).filter(i -> state.carsToBuild[i] > 0).iterator();
    }


    @Override
    public CSState transition(CSState state, Decision decision) {
        int[] nextCarsToBuild = Arrays.copyOf(state.carsToBuild, nClasses() + 1);
        nextCarsToBuild[decision.val()]--; // Built a car in class [decision.val()]
        long[] nextPreviousBlocks = new long[nOptions()];
        int[] nextNWithOption = Arrays.copyOf(state.nWithOption, nOptions());
        for (int i = 0; i < nOptions(); i++) { // Shift blocks and add new car to them
            nextPreviousBlocks[i] = (state.previousBlocks[i] << 1) & ((1L << (blockSize[i] - 1)) - 1);
            if (carOptions[decision.val()][i]) {
                nextPreviousBlocks[i] |= 1;
                nextNWithOption[i]--;
            }
        }
        return new CSState(this, nextCarsToBuild, nextPreviousBlocks, nextNWithOption, state.nToBuild - 1);
    }


    @Override
    public double transitionCost(CSState state, Decision decision) {
        double cost = 0;
        for (int i = 0; i < nOptions(); i++) { // Shift blocks and add new car to them
            if (carOptions[decision.val()][i]) {
                if (Long.bitCount(state.previousBlocks[i]) >= blockMax[i]) cost--; // Too many cars with that option recently
            }
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