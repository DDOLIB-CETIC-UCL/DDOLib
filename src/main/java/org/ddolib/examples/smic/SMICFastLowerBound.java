package org.ddolib.examples.smic;

import org.ddolib.modeling.FastLowerBound;

import java.util.BitSet;
import java.util.PriorityQueue;
import java.util.Set;
/**
 * The {@code SMICFastLowerBound} class provides a fast and simple estimation
 * of the lower bound of the remaining cost (or completion time) in the
 * Single Machine with Inventory Constraint (SMIC) scheduling problem.
 * <p>
 * This lower bound is computed based on the processing times and release dates
 * of the remaining jobs to be scheduled in a given state of the search process.
 * It is intended to provide a quick and computationally inexpensive
 * approximation to guide search algorithms such as DDO (Decision Diagram Optimization)
 * or ACS (Anytime Column Search).
 * </p>
 *
 * <p><b>Computation principle:</b></p>
 * <ul>
 *     <li>The method accumulates the total processing time of all remaining jobs.</li>
 *     <li>It also identifies the earliest release time among these jobs.</li>
 *     <li>The lower bound is then the sum of these processing times plus a correction
 *         based on the difference between the earliest release date and the current time.</li>
 * </ul>
 *
 * <p>
 * The bound does not attempt to be exact but rather provides a quick estimation
 * to help pruning suboptimal branches in the search tree.
 * </p>
 *
 * @see SMICProblem
 * @see SMICState
 * @see FastLowerBound
 */
public class SMICFastLowerBound implements FastLowerBound<SMICState> {
    /** The SMIC problem instance for which the lower bound is computed. */
    private final SMICProblem problem;
    /**
     * Constructs a fast lower bound estimator for the given SMIC problem.
     *
     * @param problem the {@link SMICProblem} instance containing job data
     *                such as processing times and release dates
     */
    SMICFastLowerBound(SMICProblem problem)  {
        this.problem = problem;
    }
    /**
     * Computes a fast lower bound for the current search state.
     * <p>
     * The lower bound is estimated as:
     * </p>
     * <pre>
     *     LB = (min(0, minRelease - currentTime)) + sum(processingTimes)
     * </pre>
     * where:
     * <ul>
     *     <li>{@code minRelease} is the smallest release time among the remaining jobs,</li>
     *     <li>{@code currentTime} is the current machine time in the state,</li>
     *     <li>{@code sum(processingTimes)} is the total processing time of all remaining jobs.</li>
     * </ul>
     * @param state the current {@link SMICState}, representing the partial schedule
     * @param variables the set of remaining decision variables (unused in this heuristic)
     * @return a lower bound estimate of the remaining cost or completion time
     */
    @Override
    public double fastLowerBound(SMICState state, Set<Integer> variables) {
        double lowerBound = 0;
        BitSet remaining = state.remainingJobs();
        PriorityQueue<Integer> queue = new PriorityQueue<>();
        for (int j = remaining.nextSetBit(0); j >= 0; j = remaining.nextSetBit(j + 1)) {
            queue.add(problem.processing[j]);
        }
        for (int i = 0; i < variables.size(); i++) {
            lowerBound += queue.poll();
        }
        return lowerBound;
    }
}
