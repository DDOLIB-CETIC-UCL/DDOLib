package org.ddolib.examples.msct;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;


/**
 * Represents a state in the {@link MSCTProblem} (Minimum Sum of Completion Times) scheduling problem.
 * <p>
 * A state captures the current status of the scheduling process:
 * it stores the set of jobs that remain to be scheduled and the
 * current simulation time (i.e., the total time elapsed so far in the partial schedule).
 * </p>
 *
 * <p><b>Structure:</b></p>
 * <ul>
 *   <li>{@code remainingJobs}: the set of jobs that have not yet been scheduled.</li>
 *   <li>{@code currentTime}: the time at which the next job can start, representing the
 *       accumulated processing time of all scheduled jobs so far.</li>
 * </ul>
 *
 * <p>
 * This class is implemented as a Java {@code record}, meaning it is immutable and
 * provides automatically generated implementations for equals, hashCode, and accessors.
 * </p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * Set<Integer> jobs = Set.of(0, 1, 2);
 * MSCTState state = new MSCTState(jobs, 5);
 * System.out.println(state);
 * // Output: RemainingJobs [0, 1, 2] ----> currentTime 5
 * }</pre>
 *
 * @param remainingJobs the set of job indices that have not yet been scheduled.
 * @param currentTime   the current time (sum of processing times of scheduled jobs).
 *
 * @see MSCTProblem
 * @see MSCTRelax
 * @see MSCTRanking
 */
public record MSCTState(Set<Integer> remainingJobs, int currentTime) {
    /**
     * Returns a string representation of the state for debugging or logging purposes.
     * <p>
     * The output includes the list of remaining jobs and the current simulation time.
     * </p>
     *
     * @return a formatted string describing this state.
     */
    @Override
    public String toString() {
        return "RemainingJobs " + Arrays.toString(remainingJobs.toArray()) + " ----> currentTime " + currentTime;
    }
}