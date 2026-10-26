package org.ddolib.nolayer.solver;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.common.util.InvalidSolutionException;
import org.ddolib.common.util.debug.DebugLevel;
import org.ddolib.layered.solving.ddo.core.Decision;
import org.ddolib.nolayer.modeling.Model;

/**
 * Interface representing a generic solver for no-layered decision diagram based optimization
 * problems.
 *
 * <p>A solver explores the search space defined by a decision diagram, applies bounds and
 * relaxations, and can return the best solution found along with its value.
 *
 * <p>Implementations of this interface typically provide algorithms such as:
 *
 * <ul>
 *   <li>Dynamic programming on decision diagrams
 *   <li>A* search
 *   <li>Branch-and-bound or anytime search strategies
 * </ul>
 *
 * @see SearchStatistics
 * @see Decision
 */
public interface Solver {

    /**
     * Minimizes the objective function according to the solver strategy.
     *
     * @param limit a {@link Predicate} that can limit or stop the search based on current {@link
     *     SearchStatistics}
     * @param onSolution a {@link BiConsumer} invoked on each new solution found; receives the
     *     solution array and current statistics
     * @return the statistics of the search after completion
     */
    Solution minimize(
            Predicate<SearchStatistics> limit,
            BiConsumer<List<Integer>, SearchStatistics> onSolution);

    /**
     * Returns the value of the best solution found so far by this solver, if any.
     *
     * @return the value of the best solution in this decision diagram if there is one
     */
    Optional<Double> bestValue();

    /**
     * Returns the ordered list of labels leading to the best solution from the initial state.
     *
     * @return the ordered list of labels leading to the best solution from the initial state
     */
    List<Integer> bestSolution();

    /**
     * Returns the initial solution of a model, if it provides a non-empty one.
     *
     * <p>Its value is trusted. When the debug mode is enabled, it is checked against {@link
     * org.ddolib.nolayer.modeling.Problem#evaluate(List)}.
     *
     * @param model the model providing the initial solution
     * @return the initial solution, or an empty {@link Optional} if the model has none
     * @throws IllegalArgumentException in debug mode, if the initial solution is invalid or if its
     *     value does not match its evaluation
     */
    static Optional<Solution> initialSolution(Model<?> model) {
        Solution init = model.initialSolution();
        if (init == null || init.isEmpty()) {
            return Optional.empty();
        }
        if (model.debugMode() != DebugLevel.OFF) {
            try {
                double evaluated = model.problem().evaluate(init.solution());
                if (Math.abs(evaluated - init.value()) > 1e-6) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "The value of the initial solution (%f) does not match its"
                                            + " evaluation (%f)",
                                    init.value(), evaluated));
                }
            } catch (InvalidSolutionException e) {
                throw new IllegalArgumentException("The initial solution is invalid", e);
            }
        }
        return Optional.of(init);
    }
}
