package org.ddolib.examples.mks;

import org.ddolib.modeling.FastLowerBound;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

public class MKSFastLowerBound implements FastLowerBound<MKSState> {

    private final MKSProblem problem;

    public MKSFastLowerBound(MKSProblem problem) {
        this.problem = problem;
    }

    @Override
    public double fastLowerBound(MKSState state, Set<Integer> variables) {
        int maxProfit = 0;
        for (Integer variable : variables) {
            maxProfit += problem.profit[variable];
        }
        return -maxProfit;
    }
}
