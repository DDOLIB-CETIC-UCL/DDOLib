package org.ddolib.examples.gruler;

import org.ddolib.modeling.StateRanking;
/**
 * Defines a ranking strategy for states in the Golomb Ruler (GR) problem.
 * <p>
 * This class provides a simple heuristic ordering between two {@link GRState} objects
 * based on the position of their last mark. It can be used by search algorithms
 * such as DDO, A*, or Anytime Column Search to prioritize exploration.
 * </p>
 *
 * <p>
 * The comparison criterion is straightforward: the state with the smaller last mark
 * (i.e., the shorter current ruler) is considered better.
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * GRRanking ranking = new GRRanking();
 * int result = ranking.compare(state1, state2);
 * // result < 0 means state1 is preferred (smaller last mark)
 * }</pre>
 *
 * @see GRState
 * @see StateRanking
 */
public class GRRanking implements StateRanking<GRState> {
    /**
     * Compares two {@link GRState} instances based on their last placed mark.
     * <p>
     * A state with a smaller {@code lastMark} value is considered better,
     * since it represents a shorter partial ruler.
     * </p>
     *
     * @param s1 the first state to compare.
     * @param s2 the second state to compare.
     * @return a negative integer if {@code s1} has a smaller last mark than {@code s2},
     *         zero if they are equal, or a positive integer otherwise.
     */
    @Override
    public int compare(GRState s1, GRState s2) {
        return Integer.compare(s1.getLastMark(), s2.getLastMark()); // sort by last mark
    }
}
