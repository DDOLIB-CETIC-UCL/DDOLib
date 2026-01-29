package org.ddolib.common.solver;

import org.ddolib.ddo.core.Decision;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Interface representing a generic solver for decision diagram based optimization problems.
 * <p>
 * A solver explores the search space defined by a decision diagram, applies bounds and relaxations,
 * and can return the best solution found along with its value.
 * </p>
 *
 * <p>
 * Implementations of this interface typically provide algorithms such as:
 * </p>
 * <ul>
 *     <li>Dynamic programming on decision diagrams</li>
 *     <li>A* search</li>
 *     <li>Branch-and-bound or anytime search strategies</li>
 * </ul>
 *
 * @see SearchStatistics
 * @see Decision
 */
public interface Solver {
    /**
     * Minimizes the objective function according to the solver strategy.
     *
     * @param limit      a {@link Predicate} that can limit or stop the search based on current {@link SearchStatistics}
     * @param onSolution a {@link BiConsumer} invoked on each new solution found; receives the solution array and
     *                   current statistics
     * @return the statistics of the search after completion
     */
    Solution minimize(Predicate<SearchStatistics> limit,
                      BiConsumer<int[], SearchStatistics> onSolution);

    /**
     * @return the value of the best solution in this decision diagram if there is one
     */
    Optional<Double> bestValue();

    /**
     * Returns the set of decisions that lead to the best solution found by this solver, if any.
     *
     * @return an {@link Optional} containing the set of {@link Decision} objects representing the best solution,
     * or empty if no solution exists
     */
    Optional<Set<Decision>> bestSolution();

    /**
     * Computes the gap (percentage difference) between the best known upper bound and the lowest
     * lower bound in the open nodes, for anytime search reporting.
     *
     * @return gap as a percentage
     */
    double gap();

    /**
     * Constructs an array representing the values assigned to each variable from a set of decisions.
     *
     * @param decisions a set of {@link Decision} objects representing variable assignments
     * @return an array {@code t} such that {@code t[i]} is the assigned value of variable {@code i},
     * or an empty array if the solution does not exist
     */
    default int[] constructSolution(Set<Decision> decisions) {
        int[] toReturn = new int[decisions.size()];
        for (Decision d : decisions) {
            toReturn[d.var()] = d.val();
        }
        return toReturn;
    }
}
