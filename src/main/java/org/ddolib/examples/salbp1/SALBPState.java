package org.ddolib.examples.salbp1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;


public record SALBPState(BitSet stations, BitSet currentStation, double remainingDuration) {
    @Override
    public String toString() {return "Stations: " + stations + " CurrentStation: " + currentStation + " RemainingDuration: " + remainingDuration;}
}

//public record SALBPState(ArrayList<BitSet> stations, BitSet currentStation, double remainingDuration) {
//    @Override
//    public String toString() {return "Stations: " + Arrays.toString(stations.toArray()) + " CurrentStation: " + currentStation + " RemainingDuration: " + remainingDuration;}
//}
