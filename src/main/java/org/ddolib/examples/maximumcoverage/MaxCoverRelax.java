package org.ddolib.examples.maximumcoverage;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;

public class MaxCoverRelax implements Relaxation<MaxCoverState> {
    final MaxCoverProblem problem;
    public MaxCoverRelax(MaxCoverProblem problem) {
        this.problem = problem;
    }
    @Override
    public MaxCoverState mergeStates(final Iterator<MaxCoverState> states) {
        MaxCoverState state = states.next();
        BitSet intersectionCoveredItems = (BitSet) state.coveredItems().clone();
        while (states.hasNext()) {
            state = states.next();
            intersectionCoveredItems.and(state.coveredItems());
        }
        return new MaxCoverState(intersectionCoveredItems);
    }

    @Override
    public double relaxEdge(MaxCoverState from, MaxCoverState to, MaxCoverState merged, Decision d, double cost) {
        return cost;
    }
}
