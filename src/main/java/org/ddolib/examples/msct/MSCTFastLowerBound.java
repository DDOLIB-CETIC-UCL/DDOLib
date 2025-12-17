package org.ddolib.examples.msct;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;
/**
 * Provides a fast lower bound computation for the Maximum Sum of Compatible Tasks (MSCT) problem.
 * <p>
 * The {@link MSCTFastLowerBound} class estimates a lower bound on the total completion time
 * (or cost) that can be achieved from a given partial state of the problem.
 * This lower bound is used within search-based optimization algorithms
 * (such as A*, ACS, or DDO) to prune suboptimal branches and guide the search toward
 * promising solutions.
 * </p>
 *
 * <p>
 * The lower bound is computed based on the remaining tasks to be scheduled and the
 * current state of the system. It takes into account the earliest possible start time
 * (release date) and the smallest processing time among the remaining tasks.
 * </p>
 *
 * <p><b>Computation logic:</b></p>
 * <ul>
 *   <li>Let {@code k} be the number of remaining tasks to schedule.</li>
 *   <li>{@code minProcessing} is the smallest processing time among the remaining tasks.</li>
 *   <li>{@code minRelease} is the smallest release time among the remaining tasks.</li>
 *   <li>{@code u} is the maximum between {@code state.currentTime()} and {@code minRelease}.</li>
 * </ul>
 * The lower bound is then estimated as:
 * <pre>
 *     LB = k * u + minProcessing * (k * (k + 1) / 2.0)
 * </pre>
 * <p>
 * This formula roughly estimates the minimal cumulative processing time that remains
 * given the current schedule state.
 * </p>
 *
 * <p><b>Example:</b></p>
 * <pre>
 * MSCTProblem problem = new MSCTProblem("data/MSCT/msct1.txt");
 * MSCTState state = new MSCTState(...);
 * MSCTFastLowerBound lb = new MSCTFastLowerBound(problem);
 *
 * double lowerBound = lb.fastLowerBound(state, Set.of(0, 1, 2));
 * System.out.println("Estimated lower bound: " + lowerBound);
 * </pre>
 */
public class MSCTFastLowerBound implements FastLowerBound<MSCTState> {
    /**
     * The MSCT problem instance for which the lower bound is computed.
     */
    private final MSCTProblem problem;

    /**
     * Constructs a lower bound evaluator for a specific instance of the MSCT problem.
     *
     * @param problem the {@link MSCTProblem} instance containing the task data
     *                (processing times, release dates, etc.).
     */
    public MSCTFastLowerBound(MSCTProblem problem) {
        this.problem = problem;
    }

    /**
     * Computes a fast lower bound on the total completion time (or cost)
     * from the given state and remaining variables.
     * <p>
     * This method evaluates how good a partial solution can potentially become by estimating
     * the minimal additional cost required to schedule the remaining tasks.
     * </p>
     *
     * @param state      the current state of the problem, representing already scheduled tasks.
     * @param variables  the set of remaining variables (tasks) to be scheduled.
     * @return an estimated lower bound on the minimal total cost achievable from the current state.
     */
    @Override
    public double fastLowerBound(MSCTState state, Set<Integer> variables) {
        int lb = 0;

        for (Integer v : state.remainingJobs()) {
            lb += Math.max(state.currentTime(), problem.release[v]) + problem.processing[v];
        }
        return lb;
    }
}
