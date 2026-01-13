package org.ddolib.examples.msct;

import org.ddolib.modeling.StateRanking;
/**
 * Provides a ranking strategy for {@link MSCTState} objects
 * used in the search process for solving the
 * <b>Maximum Sum of Completion Times (MSCT)</b> problem.
 * <p>
 * The ranking is based on the current time value of each state.
 * States with a smaller {@code currentTime} are considered
 * more promising and are therefore ranked higher (i.e., explored first).
 * </p>
 *
 * <p>
 * This ranking function is typically used in <b>Decision Diagram Optimization (DDO)</b>
 * or <b>A*</b> search algorithms to prioritize states in the frontier,
 * helping the solver guide the exploration towards better solutions faster.
 * </p>
 *
 * <p><b>Ranking policy:</b></p>
 * <ul>
 *   <li>If {@code s1.currentTime() < s2.currentTime()}, then {@code s1} is preferred (ranked first).</li>
 *   <li>If {@code s1.currentTime() > s2.currentTime()}, then {@code s2} is preferred.</li>
 *   <li>If both have the same current time, they are considered equivalent in ranking.</li>
 * </ul>
 *
 * @see MSCTState
 * @see StateRanking
 * @see MSCTDdoMain
 * @see MSCTRelax
 */
public class MSCTRanking implements StateRanking<MSCTState> {
    /**
     * Compares two scheduling states according to their current completion time.
     * <p>
     * The state with the smaller {@code currentTime} is ranked higher (considered better).
     * </p>
     *
     * @param s1 the first state to compare.
     * @param s2 the second state to compare.
     * @return a negative integer if {@code s1} should be ranked before {@code s2},
     *         zero if both states have the same rank,
     *         or a positive integer if {@code s1} should be ranked after {@code s2}.
     */
    @Override
    public int compare(MSCTState s1, MSCTState s2) {
        return Integer.compare(s1.currentTime(), s2.currentTime());
    }
}