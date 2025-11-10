package org.ddolib.examples.mks;

import java.util.Arrays;

public class MKSState {

    double[] capacities;

    public MKSState(double[] capacities) {
        this.capacities = capacities;
    }

    @Override
    public MKSState clone() {
        return new MKSState(capacities.clone());
    }

    @Override
    public String toString() {
        return Arrays.toString(capacities);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(capacities);
    }

    @Override
    public boolean equals(Object o) {
        assert o instanceof MKSState;
        return Arrays.equals(capacities, ((MKSState) o).capacities);
    }

}
