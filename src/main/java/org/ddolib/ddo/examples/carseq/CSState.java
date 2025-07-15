package org.ddolib.ddo.examples.carseq;

import java.util.Arrays;
import java.util.Objects;

public class CSState {
    public final int[] carsToBuild; // Number of cars of each class that must be built
    public final long[] previousBlocks; // For each option and each car in the block, true if the option was used previously for that car

    public CSState(int[] carsToBuild, long[] previousBlocks) {
        this.carsToBuild = carsToBuild;
        this.previousBlocks = previousBlocks;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CSState oState) {
            if (!Arrays.equals(carsToBuild, oState.carsToBuild)) return false;
            return Arrays.equals(previousBlocks, oState.previousBlocks);
        }
        else return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(carsToBuild), Arrays.hashCode(previousBlocks));
    }
}
