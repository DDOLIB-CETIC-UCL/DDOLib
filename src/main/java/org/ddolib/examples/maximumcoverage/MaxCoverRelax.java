package org.ddolib.examples.maximumcoverage;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Relaxation;

import java.util.BitSet;
import java.util.Iterator;

public class    MaxCoverRelax implements Relaxation<MaxCoverState> {
    final MaxCoverProblem problem;
    double totInsersectionSize = 0;
    int nbMerge = 0;
    int nbZeroIntersection = 0;
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

        int intersectionCard = intersectionCoveredItems.cardinality();
        totInsersectionSize += intersectionCard;
        nbMerge++;
        if (intersectionCard == 0) {
            nbZeroIntersection++;
        }
        if (nbMerge % 100 == 0) {
            // System.out.println("Average intersection size after "+nbMerge+" avg intersection size: "+(totInsersectionSize/nbMerge)+" nb zero rate: "+nbZeroIntersection/(double)nbMerge);
        }
        return new MaxCoverState(intersectionCoveredItems);
    }

    @Override
    public double relaxEdge(MaxCoverState from, MaxCoverState to, MaxCoverState merged, Decision d, double cost) {
        return cost;
    }
}
