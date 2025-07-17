package org.ddolib.ddo.examples.carseq;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class CSState {
    public final int[] carsToBuild; // Number of cars of each class that must be built
    public final long[] previousBlocks; // For each option and each car in the block, true if the option was used previously for that car
    public final int lowerBound;

    public CSState(CSProblem problem, int[] carsToBuild, long[] previousBlocks) {
        this.carsToBuild = carsToBuild;
        this.previousBlocks = previousBlocks;
        lowerBound = computeLowerBound(problem);
    }


    public int computeLowerBound(CSProblem problem) {
        // Count remaining number of cars
        int nToBuild = carsToBuild[problem.nClasses()];
        int[] nWithOption = new int[problem.nOptions()];
        for (int i = 0; i < problem.nClasses(); i++) {
            int nCars = carsToBuild[i];
            nToBuild += nCars;
            for (int j = 0; j < problem.nOptions(); j++) {
                if (problem.carOptions[i][j]) {
                    nWithOption[j] += nCars;
                }
            }
        }

        // Bound for each option separately
        int bound = 0;
        for (int i = 0; i < problem.nOptions(); i++) {
            // Count number of cars with and without the option in the previous block and in the future
            int k = problem.blockMax[i], l = problem.blockSize[i];
            int n = nToBuild + l;
            int withOption = nWithOption[i] + Long.bitCount(previousBlocks[i]);
            int withoutOption = n - withOption;

            // Compute bound
            int nReduce = n / l * (l - k) +
                    Math.max((n - l) % l - k, 0); // Number of cars without the option that can reduce the number of violations
            if (withoutOption < nReduce) {
                bound += nReduce - withoutOption;
            }
        }
        return bound;
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof CSState oState) {
            return Arrays.equals(carsToBuild, oState.carsToBuild) &&
                   Arrays.equals(previousBlocks, oState.previousBlocks);
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(carsToBuild), Arrays.hashCode(previousBlocks));
    }


    @Override
    public String toString() {
        return String.format(
            "CSState [\n\tcars : %s\n%s]",
            Arrays.stream(carsToBuild).mapToObj(String::valueOf).collect(Collectors.joining(" ")),
            IntStream.range(0, previousBlocks.length).mapToObj(i -> String.format(
                "\toption %d : %s\n", i,
                new StringBuilder(Long.toBinaryString(previousBlocks[i])).reverse()
            )).collect(Collectors.joining())
        );
    }
}