package org.ddolib.examples.smic;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public record SMICState(Set<Integer> remainingJobs, // set of jobs that remain to be scheduled
                        int currentTime, // current time
                        int minCurrentInventory,// min inventory bound (used for merging states)
                        int maxCurrentInventory) {  // max inventory bound (used for merging states)

    @Override
    public String toString() {
        return "RemainingJobs " + Arrays.toString(remainingJobs.toArray()) + " ----> currentTime " + currentTime + " ---> minCurrentInventory" + minCurrentInventory + " ---> maxCurrentInventory" + maxCurrentInventory;
    }

}


