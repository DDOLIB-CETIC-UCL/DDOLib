package org.ddolib.examples.smic;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class SMICState {

    private Set<Integer> remainingJobs;
    private int currentTime;
    private int minCurrentInventory;
    private int maxCurrentInventory;

    public SMICState(Set<Integer> remainingJobs, int currentTime, int minCurrentInventory, int maxCurrentInventory) {
        this.remainingJobs = remainingJobs;
        this.currentTime = currentTime;
        this.minCurrentInventory = minCurrentInventory;
        this.maxCurrentInventory = maxCurrentInventory;
    }

    public Set<Integer> getRemainingJobs() {
        return remainingJobs;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public int getMinCurrentInventory() {
        return minCurrentInventory;
    }

    public int getMaxCurrentInventory() {
        return maxCurrentInventory;
    }


    @Override
    public String toString() {
        return "RemainingJobs " + Arrays.toString(remainingJobs.toArray()) + " ----> currentTime " + currentTime + " ---> minCurrentInventory" + minCurrentInventory + " ---> maxCurrentInventory" + maxCurrentInventory;
    }

    @Override
    public int hashCode() {
        return Objects.hash(remainingJobs.hashCode(), currentTime, minCurrentInventory, maxCurrentInventory);
    }

    @Override
    public boolean equals(Object obj) {
        SMICState other = (SMICState) obj;
        if (this.currentTime == other.currentTime &&
            this.minCurrentInventory == other.minCurrentInventory &&
            this.maxCurrentInventory == other.maxCurrentInventory) {
            return true;
        }
        return false;
    }
}


