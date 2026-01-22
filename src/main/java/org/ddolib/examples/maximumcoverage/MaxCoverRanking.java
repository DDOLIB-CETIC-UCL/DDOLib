package org.ddolib.examples.maximumcoverage;

import org.ddolib.modeling.StateRanking;
/**
 * Ranking function for {@link MaxCoverState} used in the Maximum Coverage problem.
 *
 * <p>
 * This class implements {@link StateRanking} and orders states based on
 * the number of items they have covered. States covering fewer items
 * are considered "smaller" and ranked lower.
 */
public class MaxCoverRanking implements StateRanking<MaxCoverState> {
    /**
     * Compares two MaxCover states based on the number of items they cover.
     *
     * @param o1 the first state
     * @param o2 the second state
     * @return a negative integer, zero, or a positive integer as the first state
     *         covers fewer, equal, or more items than the second state
     */
    @Override
    public int compare(MaxCoverState o1, MaxCoverState o2) {
        return Integer.compare(o1.coveredItems().size(), o2.coveredItems().size());
    }
}
