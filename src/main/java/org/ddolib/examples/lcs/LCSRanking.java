package org.ddolib.examples.lcs;

import org.ddolib.modeling.StateRanking;
/**
 * Ranking strategy for {@link LCSState} in the Longest Common Subsequence (LCS) problem.
 * <p>
 * This class implements {@link StateRanking} to compare two LCS states. The comparison
 * is based on the sum of the positions in each string: states with smaller total positions
 * are considered "better" because they represent progress earlier in the strings.
 * </p>
 * <p>
 * It is typically used in search algorithms (ACS, A*, DDO) to prioritize states that
 * are closer to the beginning of the strings, which may lead to faster exploration of
 * potential optimal LCS solutions.
 * </p>
 */
public class LCSRanking implements StateRanking<LCSState> {
    /**
     * Compares two LCS states.
     * <p>
     * The state with the smaller sum of positions across all strings is considered
     * better and will be ranked higher.
     * </p>
     *
     * @param state1 The first LCS state to compare.
     * @param state2 The second LCS state to compare.
     * @return A negative integer if state1 is better, zero if equal, positive if state2 is better.
     */
    @Override
    public int compare(LCSState state1, LCSState state2) {
        // Best state is the one where the sum of string's position is the smallest.
        int totState1 = 0;
        int totState2 = 0;
        for (int i = 0; i < state1.position.length; i++) {
            totState1 += state1.position[i];
            totState2 += state2.position[i];
        }
        return Integer.compare(totState2, totState1);
    }
    /**
     * Indicates whether this ranking object is equal to another.
     * <p>
     * Currently always returns false since ranking objects do not maintain state.
     * </p>
     *
     * @param obj The other object to compare to.
     * @return false
     */
    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
