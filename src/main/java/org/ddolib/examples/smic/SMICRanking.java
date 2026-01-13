package org.ddolib.examples.smic;

import org.ddolib.modeling.StateRanking;
/**
 * The {@code SMICRanking} class defines a heuristic ranking criterion for
 * comparing two {@link SMICState} instances during search or optimization.
 * <p>
 * It implements the {@link StateRanking} interface, which is used by
 * decision diagram optimization (DDO) solvers or search algorithms to
 * determine the relative quality or priority of explored states.
 * </p>
 *
 * <p>
 * In this implementation, the ranking is based solely on the
 * {@code currentTime} attribute of the state — that is, the state
 * with the smaller current time is considered better (ranked higher),
 * since it represents a schedule that reaches an earlier point in time.
 * </p>
 *
 * <p>
 * This ranking can be used, for example, to expand partial schedules
 * that have progressed less in time before others, potentially leading
 * to better exploration efficiency in time-sensitive scheduling problems.
 * </p>
 *
 * @see SMICState
 * @see StateRanking
 * @see SMICProblem
 */
public class SMICRanking implements StateRanking<SMICState> {
    /**
     * Compares two {@link SMICState} instances based on their current time.
     * <p>
     * The comparison is ascending: a state with a smaller {@code currentTime}
     * is considered "better" or of higher priority.
     * </p>
     *
     * @param o1 the first state to compare
     * @param o2 the second state to compare
     * @return a negative integer if {@code o1} has a smaller current time than {@code o2},
     *         zero if they are equal, or a positive integer otherwise
     */
    @Override
    public int compare(SMICState o1, SMICState o2) {
        return Integer.compare(o1.currentTime(), o2.currentTime());
    }
}



