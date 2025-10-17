package org.ddolib.examples.misp;

import org.ddolib.modeling.FastLowerBound;

import java.util.BitSet;
import java.util.Set;

/**
 * Implementation of a fast lower bound for the MISP.
 */
public class MispFastLowerBound implements FastLowerBound<BitSet> {
    private final MispProblem problem;

    public MispFastLowerBound(MispProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(BitSet state, Set<Integer> variables) {
        // We select all the remaining nodes
        return -state.stream().map(i -> problem.weight[i]).sum();
    }
}
