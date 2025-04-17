package org.ddolib.ddo.examples.fixed;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FixedDDRelax implements Relaxation<Set<Integer>> {

    private final FixedDDProblem problem;

    public FixedDDRelax(FixedDDProblem problem) {
        this.problem = problem;
    }

    @Override
    public Set<Integer> mergeStates(Iterator<Set<Integer>> states) {
        HashSet<Integer> merged = new HashSet<Integer>();
        while (states.hasNext()) {
            Set<Integer> state = states.next();
            merged.addAll(state);
        }
        return merged;
    }

    @Override
    public int relaxEdge(Set<Integer> from, Set<Integer> to, Set<Integer> merged, Decision d, int cost) {
        return cost;
    }

    @Override
    public int fastUpperBound(Set<Integer> state, Set<Integer> variables) {
        return Integer.MAX_VALUE;
    }


}