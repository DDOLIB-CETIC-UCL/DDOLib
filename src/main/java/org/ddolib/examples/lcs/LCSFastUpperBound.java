package org.ddolib.examples.lcs;

import org.ddolib.modeling.FastUpperBound;

import java.util.Set;

/**
 * Implementation of a fast upper bound heuristic for the LCS.
 */
public class LCSFastUpperBound implements FastUpperBound<LCSState> {
    LCSProblem problem;

    public LCSFastUpperBound(LCSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastUpperBound(LCSState state, Set<Integer> variables) {
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

        return Math.min(total, minPairwiseLCS);
    }
}
