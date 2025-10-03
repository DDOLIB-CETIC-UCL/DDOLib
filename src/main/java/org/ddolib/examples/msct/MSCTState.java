package org.ddolib.examples.msct;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class MSCTState {

    protected Set<Integer> remainingJobs;
    protected int currentTime;

    public MSCTState(Set<Integer> remainingJobs, int currentTime) {
        this.remainingJobs = remainingJobs;
        this.currentTime = currentTime;
    }

    public Set<Integer> getRemainingJobs() {
        return remainingJobs;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    @Override
    public String toString() {
        return "RemainingJobs " + Arrays.toString(remainingJobs.toArray()) + " ----> currentTime " + currentTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(remainingJobs.hashCode(), currentTime);
    }
}