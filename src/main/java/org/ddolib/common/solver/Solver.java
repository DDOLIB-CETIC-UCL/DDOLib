package org.ddolib.common.solver;

import org.ddolib.ddo.core.Decision;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * This defines the expected behavior of a solver: an object able to find a solution
 * that minimizes the objective value of some underlying optimization problem.
 */
public interface Solver {

    SearchStatistics minimize(Predicate<SearchStatistics> limit,
                              BiConsumer<int[], SearchStatistics> onSolution);

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
     * @return An array {@code t} such that {@code t[i]} is the assigned value to the variable
     * {@code i}. Or empty array if the solution does not exist.
     */
    default int[] constructSolution(Set<Decision> decisions) {
        int[] toReturn = new int[decisions.size()];
        for (Decision d : decisions) {
            toReturn[d.var()] = d.val();
        }
        return toReturn;
    }
}
