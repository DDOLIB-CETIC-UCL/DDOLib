package org.ddolib.ddo.examples.tsp;


import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;

public class TSPRelax implements Relaxation<TSPState> {

    private final TSPProblem problem;

    public TSPRelax(TSPProblem problem) {
        this.problem = problem;
    }

    @Override
    public TSPState mergeStates(final Iterator<TSPState> states) {
        BitSet toVisit = new BitSet(problem.n);
        BitSet current = new BitSet(problem.n);

        while (states.hasNext()) {
            TSPState state = states.next();
            toVisit.or(state.toVisit); // union
            current.or(state.current); // union
        }

        return new TSPState(current, toVisit);
    }

    @Override
    public double relaxEdge(TSPState from, TSPState to, TSPState merged, Decision d, double cost) {
        return cost;
    }

}

