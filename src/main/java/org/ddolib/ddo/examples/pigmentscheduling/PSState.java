package org.ddolib.ddo.examples.pigmentscheduling;

import java.util.Arrays;

public class PSState {

    int t; // the current time slot
    int next; // the item type produced at time t+1, -1 means we don't know yet
    int [] previousDemands; // previousDemands[i] = largest time < t with demand for item i, -1 if no more demands

    public PSState(int t, int next, int[] previousDemands) {
        this.t = t;
        this.next = next;
        this.previousDemands = previousDemands;
    }

    @Override
    protected PSState clone() {
        return new PSState(t, next, Arrays.copyOf(previousDemands, previousDemands.length));
    }
}
