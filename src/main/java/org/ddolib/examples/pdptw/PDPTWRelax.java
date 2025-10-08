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
        //NB: the current node is normally the same in all states
        BitSet openToVisit = new BitSet(problem.n);
        BitSet current = new BitSet(problem.n);
        BitSet allToVisit = new BitSet(problem.n);
        int minContent = Integer.MAX_VALUE;
        int maxContent = Integer.MIN_VALUE;
        int minTime = Integer.MAX_VALUE;
        while (states.hasNext()) {
            PDPTWState state = states.next();
            //take the union; loose precision here
            openToVisit.or(state.openToVisit);
            allToVisit.or(state.allToVisit);
            current.or(state.current);
            minContent = Math.min(minContent, state.minContent);
            maxContent = Math.max(maxContent, state.maxContent);
            minTime = Math.min(minTime, state.currentTime);
        }

        return new PDPTWState(current, openToVisit, allToVisit,minContent,maxContent,minTime);
    }

    @Override
    public double relaxEdge(PDPTWState from, PDPTWState to, PDPTWState merged, Decision d, double cost) {

        return cost;
    }

}
