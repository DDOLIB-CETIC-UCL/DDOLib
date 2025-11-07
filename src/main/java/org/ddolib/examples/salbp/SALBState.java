package org.ddolib.examples.salbp;

import java.util.Arrays;
import java.util.BitSet;

public record SALBState (BitSet[] stations, double[] remainingDurationPerStation){

    @Override
    public String toString() {return "Stations : "+  Arrays.toString(stations) + " remaining: " + Arrays.toString(remainingDurationPerStation);}
}
