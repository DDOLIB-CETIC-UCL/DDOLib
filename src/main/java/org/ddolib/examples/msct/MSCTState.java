package org.ddolib.examples.msct;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;



public record MSCTState(Set<Integer> remainingJobs, int currentTime) {
    @Override
    public String toString() {
        return "RemainingJobs " + Arrays.toString(remainingJobs.toArray()) + " ----> currentTime " + currentTime;
    }
}