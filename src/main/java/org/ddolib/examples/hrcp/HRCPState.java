package org.ddolib.examples.hrcp;

import java.util.Arrays;

/**
 * State of the HRCP scheduling problem.
 * <p>
 * Captures everything needed to continue building a schedule:
 * <ul>
 *     <li>{@code scheduled} — bitmask of already-scheduled tasks.</li>
 *     <li>{@code tH} — wall-clock time at which the human becomes available.</li>
 *     <li>{@code tR} — wall-clock time at which the robot becomes available.</li>
 *     <li>{@code readiness} — for each task {@code k}, the earliest time it may
 *         start based on the completion times of its already-scheduled predecessors.</li>
 * </ul>
 * <p>
 * Two states are considered equal when they agree on the scheduled set, tH, tR,
 * and readiness values <em>for unscheduled tasks only</em> (readiness of
 * already-scheduled tasks is irrelevant for future decisions).
 */
public final class HRCPState {

    final long scheduled;
    final int tH;
    final int tR;
    final int[] readiness;

    /**
     * @param scheduled bitmask of already-scheduled tasks
     * @param tH        human availability time
     * @param tR        robot availability time
     * @param readiness earliest start time per task based on scheduled predecessors
     */
    public HRCPState(long scheduled, int tH, int tR, int[] readiness) {
        this.scheduled = scheduled;
        this.tH = tH;
        this.tR = tR;
        this.readiness = readiness;
    }

    /** Number of tasks already scheduled. */
    public int depth() {
        return Long.bitCount(scheduled);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HRCPState other)) return false;
        if (scheduled != other.scheduled || tH != other.tH || tR != other.tR) return false;
        for (int k = 0; k < readiness.length; k++) {
            if ((scheduled & (1L << k)) == 0) {          // unscheduled task
                if (readiness[k] != other.readiness[k]) return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = Long.hashCode(scheduled);
        h = 31 * h + tH;
        h = 31 * h + tR;
        for (int k = 0; k < readiness.length; k++) {
            if ((scheduled & (1L << k)) == 0) {
                h = 31 * h + readiness[k];
            }
        }
        return h;
    }

    @Override
    public String toString() {
        return String.format("HRCP[sched=%s, tH=%d, tR=%d, R=%s]",
                Long.toBinaryString(scheduled), tH, tR, Arrays.toString(readiness));
    }
}

