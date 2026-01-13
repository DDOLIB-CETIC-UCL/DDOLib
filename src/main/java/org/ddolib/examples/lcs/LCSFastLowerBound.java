package org.ddolib.examples.lcs;

import org.ddolib.modeling.FastLowerBound;

import java.util.Set;

/**
 * Implementation of a fast lower bound heuristic for the Longest Common Subsequence (LCS) problem.
 * <p>
 * This class provides a quick estimation of the best achievable LCS length from a given
 * {@link LCSState}. It is designed to be used within search algorithms to prune the
 * search space efficiently.
 * </p>
 * <p>
 * The heuristic works by:
 * </p>
 * <ul>
 *     <li>For each character, computing the minimum number of occurrences remaining
 *         in all strings from their current positions, contributing to a lower bound
 *         on the LCS length.</li>
 *     <li>For each string pair, using precomputed pairwise LCS tables to find the
 *         minimum LCS length achievable based on the current positions.</li>
 *     <li>Returning the negative of the smaller value between the total character-based
 *         bound and the minimum pairwise LCS bound. This aligns with the solver's
 *         convention of minimizing costs.</li>
 * </ul>
 */
public class LCSFastLowerBound implements FastLowerBound<LCSState> {
    /** The LCS problem instance associated with this heuristic. */
    LCSProblem problem;
    /**
     * Constructs a fast lower bound heuristic for a given LCS problem.
     *
     * @param problem The LCS problem instance.
     */
    public LCSFastLowerBound(LCSProblem problem) {
        this.problem = problem;
    }
    /**
     * Computes a fast lower bound on the objective function for the given state.
     *
     * @param state The current LCS state representing positions in each string.
     * @param variables The set of variables (unused in this heuristic but required by interface).
     * @return The negative of the estimated maximum LCS length achievable from this state.
     */
    @Override
    public double fastLowerBound(LCSState state, Set<Integer> variables) {
        // For each character, gets the minimal amount left in common between all strings.
        int total = 0;
        for (int c = 0; c < problem.diffCharNb; ++c) {
            int inCommon = Integer.MAX_VALUE;
            for (int s = 0; s < problem.stringNb; ++s) {
                inCommon = Math.min(inCommon, problem.remChar[s][c][state.position[s]]);
            }
            total += inCommon;
        }

        // For each string, gets the minimal pairwise LCS solution based on current string's position.
        int minPairwiseLCS = Integer.MAX_VALUE;
        for (int s = 0; s < problem.stringNb - 1; ++s) {
            minPairwiseLCS = Math.min(minPairwiseLCS, problem.tables[s][state.position[s]][state.position[s + 1]]);
        }

        return -(Math.min(total, minPairwiseLCS));
    }
}
