package org.ddolib.examples.alp;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;
/**
 * Fast lower bound computation for the <b>Aircraft Landing Problem (ALP)</b>.
 * <p>
 * This class implements the {@link FastLowerBound} interface for {@link ALPState}.
 * It provides a heuristic estimation of the minimum total tardiness that can be
 * achieved from a given state, considering only unassigned aircraft and ignoring
 * conflicts that may arise from simultaneous assignments.
 * </p>
 * <p>
 * The heuristic works as follows:
 * </p>
 * <ul>
 *     <li>For each aircraft class, it iterates over the remaining aircraft.</li>
 *     <li>For each aircraft, it computes the estimated tardiness for each runway.</li>
 *     <li>The minimum estimated tardiness across all runways is selected.</li>
 *     <li>If an aircraft cannot be assigned to any runway without missing its deadline,
 *         the function returns {@link Integer#MAX_VALUE} to indicate an infeasible state.</li>
 *     <li>The sum of these minimum tardiness values over all remaining aircraft is returned
 *         as the fast lower bound.</li>
 * </ul>
 *
 * @see FastLowerBound
 * @see ALPState
 * @see ALPProblem
 */
public class ALPFastLowerBound implements FastLowerBound<ALPState> {
    /** Reference to the ALP problem instance. */
    private final ALPProblem problem;
    /**
     * Constructs a new fast lower bound evaluator for the given ALP problem.
     *
     * @param problem the ALP problem instance
     */
    public ALPFastLowerBound(ALPProblem problem) {
        this.problem = problem;
    }
    /**
     * Computes a heuristic lower bound of total tardiness from the given state.
     *
     * @param state the current state containing remaining aircraft and runway assignments
     * @param variables the set of variables (aircraft indices) that can still be assigned
     * @return the estimated lower bound of total tardiness; {@link Integer#MAX_VALUE} if infeasible
     */
    @Override
    public double fastLowerBound(ALPState state, Set<Integer> variables) {
        int sum = 0;
        for (int c = 0; c < problem.nbClasses; c++) {
            for (int r = 0; r < state.remainingAircraftOfClass[c]; r++) {
                //rem(c) always start with a 0
                int aircraft = problem.latestToEarliestAircraftByClass.get(c).get(r + 1);
                int bestRunwayTardiness = Integer.MAX_VALUE;
                for (int runway = 0; runway < state.runwayStates.length; runway++) {
                    int arrivalTime = problem.getArrivalTime(state.runwayStates, aircraft, runway);
                    if (arrivalTime - problem.aircraftDeadline[aircraft] <= 0) {
                        // If one aircraft can not land, the fub is maxValue
                        int estimatedTardiness = Math.max(0, arrivalTime - problem.aircraftTarget[aircraft]);
                        bestRunwayTardiness = Math.min(bestRunwayTardiness, estimatedTardiness);
                    }
                }
                if (bestRunwayTardiness == Integer.MAX_VALUE) return (bestRunwayTardiness);
                sum += bestRunwayTardiness;
            }
        }

        return sum;
    }
}
