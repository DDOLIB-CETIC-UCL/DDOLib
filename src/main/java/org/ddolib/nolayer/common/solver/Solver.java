package org.ddolib.nolayer.common.solver;

import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.layered.solving.ddo.core.Decision;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Interface representing a generic solver for no-layered decision diagram based optimization problems.
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
                      BiConsumer<List<Integer>, SearchStatistics> onSolution);

    /**
     * @return the value of the best solution in this decision diagram if there is one
     */
    Optional<Double> bestValue();

    /**
     * Returns the ordered list of labels leading to the best solution from the initial state.
     *
     * @return the ordered list of labels leading to the best solution from the initial state.
     */
    List<Integer> bestSolution();

}
