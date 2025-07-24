package org.ddolib.examples.ddo.carseq;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class CSState {
    public final int[] carsToBuild; // Number of cars of each class that must be built
    public final long[] previousBlocks; // For each option and each car in the block, true if the option was used previously for that car

    public final int[] nWithOption; // For each option, number of cars with that option in previousBlocks or carsToBuild
    public final int nToBuild; // Total number of cars to build
    public final double utilizationRate; // sum(withOption / max)
    private final int hash; // Pre-computed hash code


    public CSState(CSProblem problem, int[] carsToBuild, long[] previousBlocks, int[] nWithOption, int nToBuild) {
        this.carsToBuild = carsToBuild;
        this.previousBlocks = previousBlocks;
        this.nWithOption = nWithOption;
        this.nToBuild = nToBuild;
        hash = Objects.hash(Arrays.hashCode(carsToBuild), Arrays.hashCode(previousBlocks));

        // Compute utilization rate
        double rate = 0;
        for (int i = 0; i < problem.nOptions(); i++) {
            int k = problem.blockMax[i], l = problem.blockSize[i], n = nToBuild;
            int max = n / l * k +
                Math.max(0, Math.min(n % l, problem.blockMax[i] - Long.bitCount(previousBlocks[i] & (((1L << (l - 1)) - 1) >> (n % l - 1)))));
            rate += (double)nWithOption[i] / max;
        }
        utilizationRate = rate;
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
        return hash;
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