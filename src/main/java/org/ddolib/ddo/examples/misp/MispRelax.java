package org.ddolib.ddo.examples.misp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;

public class MispRelax implements Relaxation<BitSet> {

    private final MispProblem problem;

    public MispRelax(MispProblem problem) {
        this.problem = problem;
    }

    @Override
    public BitSet mergeStates(Iterator<BitSet> states) {
        var merged = new BitSet(problem.nbVars());
        while (states.hasNext()) {
            final BitSet state = states.next();
            // the merged state is the union of all the state
            merged.or(state);
        }
        return merged;
    }

    @Override
    public double relaxEdge(BitSet from, BitSet to, BitSet merged, Decision d, double cost) {
        return cost;
    }

    @Override
    public double fastUpperBound(BitSet state, Set<Integer> variables) {
        // We select all the remaining nodes
        return state.stream().map(i -> problem.weight[i]).sum();
    }
}