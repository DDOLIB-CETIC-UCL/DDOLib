package org.ddolib.nolayer.modeling;

import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.nolayer.common.solver.Solution;
import org.ddolib.nolayer.solving.acs.core.solver.AcsSolver;
import org.ddolib.nolayer.solving.astar.core.solver.AStarSolver;
import org.ddolib.nolayer.solving.awastar.core.solver.AwAstarSolver;
import org.ddolib.nolayer.solving.ddo.core.solver.DdoSolver;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * The {@code Solvers} class acts as a unified entry point for running different
 * optimization algorithms within the no-layer Decision Diagram Optimization (DDO) framework.
 * <p>
 * Unlike {@link org.ddolib.layered.modeling.Solvers}, the problems solved here don't require a
 * fixed, known number of variables up front: termination is detected via
 * {@link Problem#isTarget(Object)} instead.
 * </p>
 * It provides static methods to solve problems using various strategies:
 * <ul>
 *   <li><b>DDO (Decision Diagram Optimization)</b> — Builds restricted or relaxed decision diagrams
 *       to approximate or find exact solutions.</li>
 *   <li><b>A*</b> — Uses a best-first search guided by an admissible lower bound heuristic
 *       to guarantee optimality.</li>
 *   <li><b>ACS (Anytime Column Search)</b> — An iterative improvement algorithm that progressively
 *       refines solutions over time using bounded-width decision diagrams.</li>
 *   <li><b>Anytime Weighted A*</b> — A variant of the A* algorithm that progressively refines a
 *       solution using a weighted heuristic function.</li>
 * </ul>
 *
 * @see DdoModel
 * @see AcsModel
 * @see AwAstarModel
 * @see Model
 * @see Solution
 * @see SearchStatistics
 * @see DdoSolver
 * @see AStarSolver
 * @see AcsSolver
 * @see AwAstarSolver
 */
public class Solvers {
    // =============================================================
    // DDO Solver Methods
    // =============================================================

    /**
     * Solves the given model using the DDO (Decision Diagram Optimization) algorithm
     * with default stopping criteria and no solution callback.
     *
     * @param model the DDO model to solve
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeDdo(DdoModel<T> model) {
        return minimizeDdo(model, s -> false, (sol, s) -> {
        });
    }

    /**
     * Solves the given model using DDO, stopping when the provided limit condition becomes true.
     *
     * @param model the DDO model to solve
     * @param limit a predicate defining the stopping criterion (e.g., max iterations, time limit)
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeDdo(DdoModel<T> model, Predicate<SearchStatistics> limit) {
        return minimizeDdo(model, limit, (sol, s) -> {
        });
    }

    /**
     * Solves the given model using DDO and triggers a callback each time a new incumbent solution is found.
     *
     * @param model      the DDO model to solve
     * @param onSolution callback executed when a new best solution is discovered
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeDdo(DdoModel<T> model, BiConsumer<List<Integer>, SearchStatistics> onSolution) {
        return minimizeDdo(model, s -> false, onSolution);
    }

    /**
     * Core method for solving a DDO model with a custom stop condition and a solution callback.
     * <p>
     * It automatically builds the solver configuration from the given model and delegates
     * the actual solving to a {@link DdoSolver}.
     * </p>
     *
     * @param model      the DDO model to solve
     * @param limit      a predicate defining when the solver should stop
     * @param onSolution a callback invoked whenever a new best solution is found
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeDdo(DdoModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<List<Integer>, SearchStatistics> onSolution) {
        return new DdoSolver<>(model).minimize(limit, onSolution);
    }

    // =============================================================
    // A* Solver Methods
    // =============================================================

    /**
     * Solves the given model using the A* search algorithm with default parameters.
     *
     * @param model the model to solve
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeAstar(Model<T> model) {
        return minimizeAstar(model, s -> false, (sol, s) -> {
        });
    }

    /**
     * Solves the given model using A* with a specified stop condition.
     *
     * @param model the model to solve
     * @param limit predicate defining the termination condition
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeAstar(Model<T> model, Predicate<SearchStatistics> limit) {
        return minimizeAstar(model, limit, (sol, s) -> {
        });
    }

    /**
     * Solves the given model using A* and calls back when new incumbent solutions are found.
     *
     * @param model      the model to solve
     * @param onSolution callback triggered for each new best solution
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeAstar(Model<T> model, BiConsumer<List<Integer>, SearchStatistics> onSolution) {
        return minimizeAstar(model, s -> false, onSolution);
    }

    /**
     * Core method for solving a model with the A* search algorithm, with custom limit and callback.
     * <p>
     * It automatically builds the solver configuration from the given model and delegates
     * the actual solving to an {@link AStarSolver}.
     * </p>
     *
     * @param model      the model to solve
     * @param limit      stopping condition for the search
     * @param onSolution callback invoked when a new best solution is found
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeAstar(Model<T> model, Predicate<SearchStatistics> limit, BiConsumer<List<Integer>, SearchStatistics> onSolution) {
        return new AStarSolver<>(model).minimize(limit, onSolution);
    }

    // =============================================================
    // Anytime Column Search (ACS) Solver Methods
    // =============================================================

    /**
     * Solves the given model using the Anytime Column Search (ACS) algorithm.
     *
     * @param model the ACS model to solve
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeAcs(AcsModel<T> model) {
        return minimizeAcs(model, s -> false, (sol, s) -> {
        });
    }

    /**
     * Solves the given model using ACS, stopping when the limit condition is satisfied.
     *
     * @param model the ACS model to solve
     * @param limit predicate defining the stopping criterion
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeAcs(AcsModel<T> model, Predicate<SearchStatistics> limit) {
        return minimizeAcs(model, limit, (sol, s) -> {
        });
    }

    /**
     * Solves the given model using ACS and calls the callback when a new incumbent is found.
     *
     * @param model      the ACS model to solve
     * @param onSolution callback executed on discovery of a new best solution
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeAcs(AcsModel<T> model, BiConsumer<List<Integer>, SearchStatistics> onSolution) {
        return minimizeAcs(model, s -> false, onSolution);
    }

    /**
     * Core method for solving an ACS model with custom stopping condition and solution callback.
     * <p>
     * It automatically builds the solver configuration from the given model and delegates
     * the actual solving to an {@link AcsSolver}.
     * </p>
     *
     * @param model      the ACS model to solve
     * @param limit      predicate defining the stopping condition
     * @param onSolution callback invoked when new incumbent solutions are found
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeAcs(AcsModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<List<Integer>, SearchStatistics> onSolution) {
        return new AcsSolver<>(model).minimize(limit, onSolution);
    }

    // =============================================================
    // Anytime Weighted A* (AWA*) Solver Methods
    // =============================================================

    /**
     * Solves the given model using the Anytime Weighted A* (AWA*) algorithm with default
     * parameters.
     *
     * @param model the AWA* model to solve
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeAwAstar(AwAstarModel<T> model) {
        return minimizeAwAstar(model, s -> false, (sol, s) -> {
        });
    }

    /**
     * Solves the given model using AWA*, stopping when the limit condition is satisfied.
     *
     * @param model the AWA* model to solve
     * @param limit predicate defining the stopping criterion
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeAwAstar(AwAstarModel<T> model, Predicate<SearchStatistics> limit) {
        return minimizeAwAstar(model, limit, (sol, s) -> {
        });
    }

    /**
     * Solves the given model using AWA* and calls the callback when a new incumbent is found.
     *
     * @param model      the AWA* model to solve
     * @param onSolution callback executed on discovery of a new best solution
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeAwAstar(AwAstarModel<T> model, BiConsumer<List<Integer>, SearchStatistics> onSolution) {
        return minimizeAwAstar(model, s -> false, onSolution);
    }

    /**
     * Core method for solving a model with the Anytime Weighted A* search algorithm, with a
     * custom limit and callback.
     * <p>
     * It automatically builds the solver configuration from the given model and delegates
     * the actual solving to an {@link AwAstarSolver}.
     * </p>
     *
     * @param model      the AWA* model to solve
     * @param limit      stopping condition for the search
     * @param onSolution callback invoked when a new best solution is found
     * @return a solution to the related problem with search statistics summarizing the solver's performance
     */
    public static <T> Solution minimizeAwAstar(AwAstarModel<T> model, Predicate<SearchStatistics> limit, BiConsumer<List<Integer>, SearchStatistics> onSolution) {
        return new AwAstarSolver<>(model).minimize(limit, onSolution);
    }
}
