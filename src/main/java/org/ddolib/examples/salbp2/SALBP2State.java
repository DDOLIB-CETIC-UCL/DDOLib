package org.ddolib.examples.salbp2;

import java.util.Arrays;
import java.util.BitSet;

public record SALBP2State (BitSet[] stations, double[] cyclePerStation, double cycle) {
    @Override
    public String toString() {
        return "Stations " + Arrays.toString(stations) + "  cyclePerStation " + Arrays.toString(cyclePerStation) + " cycle " + cycle;
    }
}
