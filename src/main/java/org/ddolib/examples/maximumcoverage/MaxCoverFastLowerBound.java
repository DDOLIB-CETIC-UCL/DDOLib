package org.ddolib.examples.maximumcoverage;

import org.ddolib.modeling.FastLowerBound;

import java.util.BitSet;
import java.util.Set;

public class MaxCoverFastLowerBound implements FastLowerBound<MaxCoverState> {
    private final MaxCoverProblem problem;
    int maxCardSet = 0;
    public MaxCoverFastLowerBound(MaxCoverProblem problem) {
        this.problem = problem;
        for (BitSet subset : problem.subSets) {
            int card = subset.cardinality();
            if (card > maxCardSet) {
                maxCardSet = card;
            }
        }
    }

    @Override
    public double fastLowerBound(MaxCoverState state, Set<Integer> variables) {
        int coveredItems = state.coveredItems().cardinality();
        coveredItems += variables.size() * maxCardSet;
        return -coveredItems;
    }
}
