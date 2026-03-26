package org.ddolib.examples.ssalbrb;

import java.util.List;
import java.util.Objects;

/**
 * State representation for DP model with encoding scheme:
 * - E_t >= 0: task t is unassigned, E_t is earliest start time
 * - E_t < 0: task t is assigned, completion time is -E_t
 * 
 * State: ⟨r_h, r_r, E⟩
 */
public record SSALBRBState(
        int humanAvailable,
        int robotAvailable,
        List<Integer> earliestStartTimes) {

    public SSALBRBState {
        Objects.requireNonNull(earliestStartTimes, "earliestStartTimes");
        earliestStartTimes = List.copyOf(earliestStartTimes);
    }

    public int makespan() {
        return Math.max(humanAvailable, robotAvailable);
    }

    /**
     * Check if a task is unassigned.
     */
    public boolean isUnassigned(int task) {
        return earliestStartTimes.get(task) >= 0;
    }

    /**
     * Get the completion time of an assigned task.
     * @throws IllegalStateException if task is not assigned
     */
    public int getCompletionTime(int task) {
        int value = earliestStartTimes.get(task);
        if (value >= 0) {
            throw new IllegalStateException("Task " + task + " is not assigned yet");
        }
        return -value;
    }

    @Override
    public String toString() {
        return "⟨rh=" + humanAvailable
                + ", rr=" + robotAvailable
                + ", E=" + earliestStartTimes + "⟩";
    }
}
