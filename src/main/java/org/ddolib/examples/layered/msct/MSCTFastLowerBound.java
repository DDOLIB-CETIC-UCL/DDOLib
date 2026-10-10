package org.ddolib.examples.layered.msct;

import java.util.Set;
import org.ddolib.layered.modeling.FastLowerBound;

/**
 * Provides a fast lower bound computation for the Maximum Sum of Compatible Tasks (MSCT) problem.
 *
 * <p>The {@link MSCTFastLowerBound} class estimates a lower bound on the total completion time (or
 * cost) that can be achieved from a given partial state of the problem. This lower bound is used
 * within search-based optimization algorithms (such as A*, ACS, or DDO) to prune suboptimal
 * branches and guide the search toward promising solutions.
 *
 * <p><b>Computation logic:</b> a job {@code v} scheduled from the current state cannot complete
 * before {@code max(currentTime, release[v]) + processing[v]}. Exactly {@code k} more jobs are
 * scheduled, {@code k} being the number of remaining variables, so the bound is the sum of the
 * {@code k} smallest of these completion times.
 *
 * <p>Note that on a merged (relaxed) state, the set of remaining jobs may contain more than {@code
 * k} jobs: summing the completion times of all of them would overestimate the remaining cost.
 *
 * <p><b>Example:</b>
 *
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
    /** The MSCT problem instance for which the lower bound is computed. */
    private final MSCTProblem problem;

    /**
     * Constructs a lower bound evaluator for a specific instance of the MSCT problem.
     *
     * @param problem the {@link MSCTProblem} instance containing the task data (processing times,
     *     release dates, etc.).
     */
    public MSCTFastLowerBound(MSCTProblem problem) {
        this.problem = problem;
    }

    /**
     * Computes a fast lower bound on the total completion time (or cost) from the given state and
     * remaining variables.
     *
     * <p>This method evaluates how good a partial solution can potentially become by estimating the
     * minimal additional cost required to schedule the remaining tasks.
     *
     * @param state the current state of the problem, representing already scheduled tasks
     * @param variables the set of remaining variables (tasks) to be scheduled
     * @return an estimated lower bound on the minimal total cost achievable from the current state
     */
    @Override
    public double fastLowerBound(MSCTState state, Set<Integer> variables) {
        // earliest completion time of each remaining job
        int[] completions =
                state.remainingJobs().stream()
                        .mapToInt(
                                v ->
                                        Math.max(state.currentTime(), problem.release[v])
                                                + problem.processing[v])
                        .sorted()
                        .toArray();
        // only variables.size() jobs are still scheduled: keep the smallest completion times
        double lb = 0;
        for (int i = 0; i < Math.min(variables.size(), completions.length); i++) {
            lb += completions[i];
        }
        return lb;
    }
}
