package org.ddolib.layered.solver;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.util.PrettyPrint;
import org.ddolib.layered.solving.ddo.core.Decision;

/** Wrapper defining a solution from a set of decisions. */
public class Solution {

    private final int[] solution;
    private final SearchStatistics statistics;

    /**
     * Constructs a solution given a set of decision and {@link SearchStatistics} on this set.
     *
     * @param decisions the set of decision leading to this solution
     * @param statistics the statistics related to this solution
     */
    public Solution(Optional<Set<Decision>> decisions, SearchStatistics statistics) {
        if (decisions.isPresent()) {
            int maxVar = -1;
            for (Decision d : decisions.get()) {
                if (d.variable() > maxVar) {
                    maxVar = d.variable();
                }
            }
            if (maxVar == -1) {
                solution = new int[0];
            } else {
                solution = new int[maxVar + 1];
                for (Decision d : decisions.get()) {
                    solution[d.variable()] = d.value();
                }
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
     *     variable {@code x_i}.
     */
    public int[] solution() {
        return solution;
    }

    /**
     * Returns whether this solution is empty, i.e. no solution was found.
     *
     * @return {@code true} if this solution does not assign any variable
     */
    public boolean isEmpty() {
        return solution.length == 0;
    }

    /**
     * Returns the decisions assigning each variable to its value in this solution.
     *
     * @return the set of decisions {@code (i, t[i])} where {@code t} is {@link #solution()}
     */
    public Set<Decision> decisions() {
        Set<Decision> decisions = new HashSet<>();
        for (int i = 0; i < solution.length; i++) {
            decisions.add(new Decision(i, solution[i]));
        }
        return decisions;
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
     * @return a readable string for the search time needed to find this solution
     */
    public String searchTime() {
        return PrettyPrint.formatMs(statistics.runtime());
    }

    @Override
    public String toString() {
        return "Solution: " + Arrays.toString(solution) + "\n" + "Value: " + value();
    }
}
