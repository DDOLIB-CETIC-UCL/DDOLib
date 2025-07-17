package org.ddolib.ddo.examples.lcs;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.Arrays;
import java.util.Iterator;

public class LCSRelax implements Relaxation<LCSState> {

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
}
