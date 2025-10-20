package org.ddolib.modeling;

import org.ddolib.astar.core.solver.ACSSolver;
import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.solver.ExactSolver;
import org.ddolib.ddo.core.solver.SequentialSolver;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * The {@code Solvers} class acts as a unified entry point for running different
 * optimization algorithms within the Decision Diagram Optimization (DDO) framework.
 * <p>
 * It provides methods to solve problems using various strategies:
 * </p>
 * <ul>
 *   <li><b>DDO (Decision Diagram Optimization)</b> — Builds restricted or relaxed decision diagrams
 *       layer by layer to approximate or find exact solutions.</li>
 *   <li><b>A*</b> — Uses a best-first search guided by an admissible lower bound heuristic
 *       to guarantee optimality.</li>
 *   <li><b>ACS (Anytime Column Search)</b> — An iterative improvement algorithm that progressively
 *       refines solutions over time using bounded-width decision diagrams.</li>
 * </ul>
 *
 *
 * <p>
 * Each algorithm can be configured through its respective model type:
 * {@link DdoModel}, {@link Model}, or {@link AcsModel}.
 * The solver automatically extracts all relevant configuration elements
 * (problem definition, heuristics, relaxations, etc.) from the provided model.
 * </p>
 *
 * @param <T> the type representing the problem's state
 * @see DdoModel
 * @see AcsModel
 * @see Model
 * @see SearchStatistics
 * @see SequentialSolver
 * @see AStarSolver
 * @see ACSSolver
 */
public class Solvers<T> {
    // =============================================================
    // DDO Solver Methods
    // =============================================================

    /**
     * Solves the given model using the DDO (Decision Diagram Optimization) algorithm
     * with default stopping criteria and no solution callback.
     *
     * @param model the DDO model to solve
     * @return search statistics summarizing the solver's execution
     */
    public final SearchStatistics minimizeDdo(DdoModel<T> model) {
        return minimizeDdo(model, stats -> false, (sol, s) -> {
        });
    }

    /**
     * Solves the given model using DDO, stopping when the provided limit condition becomes true.
     *
     * @param model the DDO model to solve
     * @param limit a predicate defining the stopping criterion (e.g., max iterations, time limit)
     * @return search statistics summarizing the solver's execution
     */

    public final SearchStatistics minimizeDdo(DdoModel<T> model, Predicate<SearchStatistics> limit) {
        return minimizeDdo(model, limit, (sol, s) -> {
        });
    }

    /**
     * Solves the given model using DDO and triggers a callback each time a new incumbent solution is found.
     *
     * @param model      the DDO model to solve
     * @param onSolution callback executed when a new best solution is discovered
     * @return search statistics summarizing the solver's execution
     */

    public final SearchStatistics minimizeDdo(DdoModel<T> model, BiConsumer<Set<Decision>, SearchStatistics> onSolution) {
        return minimizeDdo(model, s -> false, onSolution);
    }

    /**
     * Core method for solving a DDO model with a custom stop condition and a solution callback.
     * <p>
     * It automatically builds the solver configuration from the given model and delegates
     * the actual solving to a {@link SequentialSolver}.
     * </p>
     *
     * @param model      the DDO model to solve
     * @param limit      a predicate defining when the solver should stop
     * @param onSolution a callback invoked whenever a new best solution is found
     * @return search statistics summarizing the solver's performance
     */

    public final SearchStatistics minimizeDdo(DdoModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<Set<Decision>, SearchStatistics> onSolution) {
        return new SequentialSolver<>(model).minimize(limit, onSolution);
    }
    // =============================================================
    // A* Solver Methods
    // =============================================================

    /**
     * Solves the given model using the A* search algorithm with default parameters.
     *
     * @param model the model to solve
     * @return search statistics summarizing the A* execution
     */

    public final SearchStatistics minimizeAstar(Model<T> model) {
        return minimizeAstar(model, s -> false, (sol, s) -> {
        });
    }

    /**
     * Solves the given model using A* with a specified stop condition.
     *
     * @param model the model to solve
     * @param limit predicate defining the termination condition
     * @return search statistics summarizing the A* execution
     */

    public final SearchStatistics minimizeAstar(Model<T> model, Predicate<SearchStatistics> limit) {
        return minimizeAstar(model, limit, (sol, s) -> {
        });
    }

    /**
     * Solves the given model using A* and calls back when new incumbent solutions are found.
     *
     * @param model      the model to solve
     * @param onSolution callback triggered for each new best solution
     * @return search statistics summarizing the A* execution
     */

    public final SearchStatistics minimizeAstar(Model<T> model, BiConsumer<Set<Decision>, SearchStatistics> onSolution) {
        return minimizeAstar(model, s -> false, onSolution);
    }

    /**
     * Core method for solving a model with the A* search algorithm, with custom limit and callback.
     *
     * @param model      the model to solve
     * @param limit      stopping condition for the search
     * @param onSolution callback invoked when a new best solution is found
     * @return search statistics of the A* execution
     */

    public final SearchStatistics minimizeAstar(Model<T> model, Predicate<SearchStatistics> limit, BiConsumer<Set<Decision>, SearchStatistics> onSolution) {
        return new AStarSolver<>(model).minimize(limit, onSolution);
    }
    // =============================================================
    // Anytime Column Search (ACS) Solver Methods
    // =============================================================

    /**
     * Solves the given model using the Anytime Column Search (ACS) algorithm.
     *
     * @param model the ACS model to solve
     * @return search statistics summarizing the ACS execution
     */

    public SearchStatistics minimizeAcs(AcsModel<T> model) {
        return minimizeAcs(model, s -> false, (sol, s) -> {
        });
    }

    /**
     * Solves the given model using ACS, stopping when the limit condition is satisfied.
     *
     * @param model the ACS model to solve
     * @param limit predicate defining the stopping criterion
     * @return search statistics summarizing the ACS execution
     */

    public SearchStatistics minimizeAcs(AcsModel<T> model, Predicate<SearchStatistics> limit) {
        return minimizeAcs(model, limit, (sol, s) -> {
        });
    }

    /**
     * Solves the given model using ACS and calls the callback when a new incumbent is found.
     *
     * @param model      the ACS model to solve
     * @param onSolution callback executed on discovery of a new best solution
     * @return search statistics summarizing the ACS execution
     */

    public SearchStatistics minimizeAcs(AcsModel<T> model, BiConsumer<Set<Decision>, SearchStatistics> onSolution) {
        return minimizeAcs(model, s -> false, onSolution);
    }

    /**
     * Core method for solving an ACS model with custom stopping condition and solution callback.
     * <p>
     * The method configures and delegates the solving process to an {@link ACSSolver}.
     * </p>
     *
     * @param model      the ACS model to solve
     * @param limit      predicate defining the stopping condition
     * @param onSolution callback invoked when new incumbent solutions are found
     * @return search statistics summarizing the ACS execution
     */

    public SearchStatistics minimizeAcs(AcsModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<Set<Decision>, SearchStatistics> onSolution) {

        return new ACSSolver<>(model).minimize(limit, onSolution);
    }

    public SearchStatistics minimizeExact(DdoModel<T> model) {

        return new ExactSolver<>(model).minimize(s -> false, (sol, s) -> {
        });
    }
}
