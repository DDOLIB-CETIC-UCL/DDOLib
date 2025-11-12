package org.ddolib.examples.salbp1;

import java.util.BitSet;


public record SALBPState(BitSet remainingTasks, BitSet currentStation, double remainingDuration) {
    @Override
    public String toString() {return "Remaining Tasks: " + remainingTasks + " CurrentStation: " + currentStation + " RemainingDuration: " + remainingDuration;}
}
