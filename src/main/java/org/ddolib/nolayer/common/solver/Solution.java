package org.ddolib.nolayer.common.solver;

import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.util.PrettyPrint;

import java.util.List;

/**
 * Wrapper defining a solution for a no-layer problem
 *
 */
public class Solution {

    private final List<Integer> solution;
    private final SearchStatistics statistics;

    public Solution(List<Integer> solution, SearchStatistics statistics) {
        this.solution = solution;
        this.statistics = statistics;
    }

    /**
     * Returns the evaluation of the objective value of this solution.
     *
     * @return the evaluation of the objective value of this solution
     */
    public double value() {
        return statistics.incumbent();
    }

    /**
     * Returns the ordered list of labels leading to the best solution from the initial state.
     *
     * @return the ordered list of labels leading to the best solution from the initial state.
     */
    public List<Integer> solution() {
        return solution;
    }

    /**
     * Returns the statistics associated to this solution.
     *
     * @return the statistics associated to this solution
     */
    public SearchStatistics statistics() {
        return statistics;
    }

    /**
     * Returns a readable string for the search time needed to find this solution.
     *
     * @return a readable string for the search time needed to find this solution.
     */
    public String searchTime() {
        return PrettyPrint.formatMs(statistics.runtime());
    }

    @Override
    public String toString() {
        return "Solution: %s%nValue: %s".formatted(solution, value());
    }
}
