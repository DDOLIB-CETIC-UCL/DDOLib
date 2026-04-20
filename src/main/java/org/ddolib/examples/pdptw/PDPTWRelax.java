package org.ddolib.examples.pdptw;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;

class PDPTWRelax implements Relaxation<PDPTWState> {
    private final PDPTWProblem problem;

    public PDPTWRelax(PDPTWProblem problem) {
        this.problem = problem;
    }

    @Override
    public PDPTWState mergeStates(final Iterator<PDPTWState> states) {
        BitSet openToVisit = new BitSet(problem.n);
        BitSet current = new BitSet(problem.n);
        BitSet allToVisit = new BitSet(problem.n);
        int minContent = Integer.MAX_VALUE;
        int maxContent = Integer.MIN_VALUE;
        double minCurrentTime = Double.MAX_VALUE;
        double maxCurrentTime = Double.MIN_VALUE;
        while (states.hasNext()) {
            PDPTWState state = states.next();
            //take the union; loose precision here
            openToVisit.or(state.openToVisit);
            allToVisit.or(state.allToVisit);
            current.or(state.current);
            minContent = Math.min(minContent, state.minContent);
            maxContent = Math.max(maxContent, state.maxContent);
            minCurrentTime = Math.min(minCurrentTime, state.minCurrentTime);
            maxCurrentTime = Math.max(maxCurrentTime, state.maxCurrentTime);
        }

        return new PDPTWState(current, openToVisit, allToVisit, minContent, maxContent, minCurrentTime, maxCurrentTime);
    }

    @Override
    public double relaxEdge(PDPTWState from, PDPTWState to, PDPTWState merged, Decision d, double cost) {
        return cost;
    }

}
