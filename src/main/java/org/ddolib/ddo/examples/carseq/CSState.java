package org.ddolib.ddo.examples.carseq;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @param carsToBuild    Number of cars of each class that must be built
 * @param previousBlocks For each option and each car in the block, true if the option was used previously for that car
 */
public record CSState(int[] carsToBuild, long[] previousBlocks) {
    @Override
    public boolean equals(Object o) {
        if (o instanceof CSState(int[] oCarsToBuild, long[] oPreviousBlocks)) {
            return Arrays.equals(carsToBuild, oCarsToBuild) &&
                   Arrays.equals(previousBlocks, oPreviousBlocks);
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
