package org.ddolib.examples.ddo.pigmentscheduling;

import java.util.Arrays;
import java.util.Objects;

public class PSState {

    int t; // the current time slot
    int next; // the item type produced at time t+1, -1 means we don't know yet
    int[] previousDemands; // previousDemands[i] = largest time < t with demand for item i, -1 if no more demands

    public PSState(int t, int next, int[] previousDemands) {
        this.t = t;
        this.next = next;
        this.previousDemands = previousDemands;
    }

    @Override
    protected PSState clone() {
        return new PSState(t, next, Arrays.copyOf(previousDemands, previousDemands.length));
    }

    @Override
    public int hashCode() {
        return Objects.hash(t, next, Arrays.hashCode(previousDemands));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PSState other) {
            return this.t == other.t
                    && this.next == other.next
                    && Arrays.equals(this.previousDemands, other.previousDemands);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("t: %d - next: %d - previousDemand: %s", t, next, Arrays.toString(previousDemands));
    }
}
