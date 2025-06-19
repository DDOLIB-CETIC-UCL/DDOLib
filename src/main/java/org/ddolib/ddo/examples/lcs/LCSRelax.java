package org.ddolib.ddo.examples.lcs;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class LCSRelax extends Relaxation<LCSState> {

    LCSProblem problem;

    public LCSRelax(LCSProblem problem) {
        this.problem = problem;
    }

    @Override
    public LCSState mergeStates(Iterator<LCSState> states) {
        int[] position = Arrays.copyOf(problem.stringsLength, problem.stringsLength.length);

        // Merged LCSState keeps the earliest position of each String.
        while (states.hasNext()) {
            LCSState state = states.next();
            for (int i = 0; i < problem.stringNb; i++) {
                position[i] = Math.min(position[i], state.position[i]);
            }
        }

        return new LCSState(position);
    }

    @Override
    public double relaxEdge(LCSState from, LCSState to, LCSState merged, Decision d, double cost) {
        return cost;
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
