package org.ddolib.ddo.examples.smic;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class SMICState {

    private Set<Integer> remainingJobs;
    private int currentTime;
    private int currentInventory;

    public SMICState(Set<Integer> remainingJobs, int currentTime, int currentInventory) {
        this.remainingJobs = remainingJobs;
        this.currentTime = currentTime;
        this.currentInventory = currentInventory;
    }

    public Set<Integer> getRemainingJobs() {
        return remainingJobs;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public int getCurrentInventory() {
        return currentInventory;
    }



    @Override
    public String toString() {
        return "RemainingJobs " + Arrays.toString(remainingJobs.toArray()) + " ----> currentTime " + currentTime + " ---> currentInventory"+ currentInventory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(remainingJobs.hashCode(),currentTime,currentInventory);
    }
}


