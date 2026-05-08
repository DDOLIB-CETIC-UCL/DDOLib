package org.ddolib.examples.salbp;

import java.util.*;

public record SALBPState(
        int currentStationRemainingTime,
        BitSet remainingTasks) {


    @Override
    public String toString() {
        return String.format("\nCurrent bin space : %d\nRemaining items : %s",
                currentStationRemainingTime, remainingTasks.toString());
    }
}