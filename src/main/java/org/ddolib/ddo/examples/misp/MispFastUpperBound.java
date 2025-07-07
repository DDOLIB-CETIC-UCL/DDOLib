package org.ddolib.ddo.examples.misp;

import org.ddolib.ddo.heuristics.FastUpperBoundHeuristic;

import java.util.BitSet;
import java.util.Set;

/**
 * Implementation of a fast upper bound for the MISP.
 */
public class MispFastUpperBound implements FastUpperBoundHeuristic<BitSet> {
    private final MispProblem problem;

    public MispFastUpperBound(MispProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastUpperBound(BitSet state, Set<Integer> variables) {
        // We select all the remaining nodes
        return state.stream().map(i -> problem.weight[i]).sum();
    }
}
