package org.ddolib.common.solver;

import org.ddolib.ddo.core.Decision;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class Solution {


    private final int[] solution;
    private final SearchStatistics statistics;


    public Solution(Optional<Set<Decision>> decisions, SearchStatistics statistics) {
        if (decisions.isPresent()) {
            solution = new int[decisions.get().size()];
            for (Decision d : decisions.get()) {
                solution[d.var()] = d.val();
            }
        } else {
            solution = new int[0];
        }

        this.statistics = statistics;
    }

    public double value() {
        return statistics.incumbent();
    }

    public int[] solution() {
        return solution;
    }

    public SearchStatistics statistics() {
        return statistics;
    }

    @Override
    public String toString() {
        return "Solution: " + Arrays.toString(solution) + "\n" + "Value: " + value();
    }
}
