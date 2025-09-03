package org.ddolib.common.solver;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.profiling.SearchStatistics;

import java.util.Optional;
import java.util.Set;

/**
 * This defines the expected behavior of a solver: an object able to find a solution
 * that maximizes the objective value of some underlying optimization problem.
 */
public interface Solver {
    /**
     * Tries to maximize the objective value of the problem which is being solved.
     *
     * @return statistics about the search
     */
    SearchStatistics maximize();

    /**
     * @return the value of the best solution in this decision diagram if there is one
     */
    Optional<Double> bestValue();

    /**
     * @return the solution leading to the best solution in this decision diagram (if it exists)
     */
    Optional<Set<Decision>> bestSolution();

    /**
     * Constructs an array containing the values assigned to each variable from the taken decisions.
     *
     * @param numVar The number of variables in the solved problem.
     * @return An array {@code t} such that {@code t[i]} is the assigned value to the variable
     * {@code i}. Or empty array if the solution does not exist.
     */
    default int[] constructBestSolution(int numVar) {
        return bestSolution().map(decisions -> {
            int[] toReturn = new int[numVar];
            for (Decision d : decisions) {
                toReturn[d.var()] = d.val();
            }
            return toReturn;
        }).orElse(new int[0]);
    }
}
