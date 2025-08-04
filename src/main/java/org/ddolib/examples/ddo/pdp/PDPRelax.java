package org.ddolib.examples.ddo.pdp;

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
        //NB: the current node is normally the same in all states
        BitSet openToVisit = new BitSet(problem.n);
        BitSet current = new BitSet(problem.n);
        BitSet allToVisit = new BitSet(problem.n);

        while (states.hasNext()) {
            PDPState state = states.next();
            //take the union; loose precision here
            openToVisit.or(state.openToVisit);
            allToVisit.or(state.allToVisit);
            current.or(state.current);
        }
        //the heuristics is reset to the initial sorted edges and will be filtered again from scratch
        return new PDPState(current, openToVisit, allToVisit);
    }

    @Override
    public double relaxEdge(PDPState from, PDPState to, PDPState merged, Decision d, double cost) {
        return cost;
    }

}
