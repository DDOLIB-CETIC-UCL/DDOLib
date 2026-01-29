package org.ddolib.common.solver;

import org.ddolib.ddo.core.Decision;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Wrapper defining a solution from a set of decisions
 *
 */
public class Solution {

    private final int[] solution;
    private final SearchStatistics statistics;


    /**
     * Constructs a solution given a set of decision and {@link SearchStatistics} on this set.
     *
     * @param decisions  the set of decision leading to this solution
     * @param statistics the statistics related to this solution
     */
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

    /**
     * Returns the evaluation of the objective value of this solution.
     *
     * @return the evaluation of the objective value of this solution
     */
    public double value() {
        return statistics.incumbent();
    }

    /**
     * Returns an array {@code t} such that {@code t[i]} is the assigned value to the decision
     * variable {@code x_i}.
     *
     * @return an array {@code t} such that {@code t[i]} is the assigned value to the decision
     * variable {@code x_i}.
     */
    public int[] solution() {
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

    @Override
    public String toString() {
        return "Solution: " + Arrays.toString(solution) + "\n" + "Value: " + value();
    }
}
