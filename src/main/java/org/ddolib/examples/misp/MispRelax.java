package org.ddolib.examples.misp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;

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
}