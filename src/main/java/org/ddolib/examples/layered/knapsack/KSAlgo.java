package org.ddolib.examples.layered.knapsack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.ddolib.common.solver.stat.AstarStats;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.layered.solver.Solution;
import org.ddolib.layered.solving.ddo.core.Decision;

/** Utility class providing algorithms to solve the Knapsack Problem. */
public class KSAlgo {

    private KSAlgo() {}

    /**
     * Provides a greedy approximation for the Knapsack Problem.
     *
     * <p>The algorithm sorts items in descending order of their profit-to-weight ratio ({@code
     * profit / weight}) and selects items sequentially as long as they fit within the remaining
     * capacity.
     *
     * @param problem the Knapsack Problem instance to solve
     * @return the selected items ({@code 1} if item {@code i} is taken, {@code 0} otherwise). As the
     *     problem is modeled as a minimization, its value is minus the total profit and provides
     *     an upper bound on the optimal value.
     */
    public static Solution greedyKS(KSProblem problem) {
        long start = System.currentTimeMillis();
        Integer[] items = new Integer[problem.nbVars()];
        for (int i = 0; i < problem.nbVars(); i++) {
            items[i] = i;
        }

        Arrays.sort(
                items,
                (o1, o2) -> {
                    double ratio1 = (double) problem.profit[o1] / problem.weight[o1];
                    double ratio2 = (double) problem.profit[o2] / problem.weight[o2];
                    return Double.compare(ratio2, ratio1);
                });

        int capa = problem.capa;
        int value = 0;
        Set<Decision> decisions = new HashSet<>();
        for (Integer i : items) {
            if (capa < problem.weight[i]) {
                decisions.add(new Decision(i, 0));
                continue;
            }
            decisions.add(new Decision(i, 1));
            value += problem.profit[i];
            capa -= problem.weight[i];
        }

        AstarStats stats = new AstarStats(start, -value);
        return new Solution(
                Optional.of(decisions),
                stats.updateTime(System.currentTimeMillis()).updateStatus(SearchStatus.SAT));
    }
}
