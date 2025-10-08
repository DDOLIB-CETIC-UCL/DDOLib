package org.ddolib.examples.pdp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;

class PDPRelax implements Relaxation<PDPState> {
    private final PDPProblem problem;

    public PDPRelax(PDPProblem problem) {
        this.problem = problem;
    }

    @Override
    public PDPState mergeStates(final Iterator<PDPState> states) {
        BitSet openToVisit = new BitSet(problem.n);
        BitSet current = new BitSet(problem.n);
        BitSet allToVisit = new BitSet(problem.n);
        int minContent = Integer.MAX_VALUE;
        int maxContent = Integer.MIN_VALUE;

        while (states.hasNext()) {
            PDPState state = states.next();
            //take the union; loose precision here
            openToVisit.or(state.openToVisit);
            allToVisit.or(state.allToVisit);
            current.or(state.current);
            minContent = Math.min(minContent, state.minContent);
            maxContent = Math.max(maxContent, state.maxContent);
        }
        //the heuristics is reset to the initial sorted edges and will be filtered again from scratch
        return new PDPState(current, openToVisit, allToVisit,minContent,maxContent);
    }

    @Override
    public double relaxEdge(PDPState from, PDPState to, PDPState merged, Decision d, double cost) {
        return cost;
    }

}
