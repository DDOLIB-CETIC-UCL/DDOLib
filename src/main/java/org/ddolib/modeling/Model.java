package org.ddolib.modeling;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;

/**
 * Defines the core model interface for describing an optimization problem to be
 * solved within the Decision Diagram Optimization (DDO) framework.
 * <p>
 * A {@code Model} encapsulates all components required to define, evaluate,
 * and guide the resolution of an optimization problem. It specifies the
 * {@link Problem} instance to solve and optionally provides custom heuristics,
 * dominance relations, and debugging or verbosity configurations.
 * </p>
 *
 * <p>
 * Implementations of this interface typically serve as the entry point for
 * configuring solvers such as {@link org.ddolib.ddo.core.solver.ExactSolver} or
 * {@link org.ddolib.ddo.core.solver.SequentialSolver}. Users can override the
 * default methods to customize behavior such as lower bound evaluation,
 * variable selection heuristics, or dominance checking.
 * </p>
 *
 * @param <T> the type representing the state space of the problem
 */
public interface Model<T> {
    /**
     * Returns the optimization problem instance associated with this model.
     *
     * @return the {@link Problem} defining the structure, transitions,
     * and objective function of the optimization task
     */
    Problem<T> problem();
    /**
     * Returns a heuristic that estimates a lower bound on the objective value
     * for a given state.
     * <p>
     * By default, this method provides a {@link DefaultFastLowerBound} instance,
     * which can be overridden for problem-specific bound estimation.
     * </p>
     *
     * @return the {@link FastLowerBound} heuristic used to compute lower bounds
     */
    default FastLowerBound<T> lowerBound() {
        return new DefaultFastLowerBound<>();
    }
    /**
     * Returns the dominance checker used to prune dominated states from the search space.
     * <p>
     * By default, this method provides a {@link DefaultDominanceChecker} instance,
     * which can be replaced by custom dominance logic tailored to the problem.
     * </p>
     *
     * @return the {@link DominanceChecker} used for dominance testing
     */
    default DominanceChecker<T> dominance() {
        return new DefaultDominanceChecker<>();
    }
    /**
     * Callback invoked when a new solution is found during the search process.
     * <p>
     * This method is a hook for monitoring or logging purposes and does nothing by default.
     * </p>
     *
     * @param statistics a snapshot of the search statistics at the time the solution was found
     */
    default void onSolution(SearchStatistics statistics) {
    }
    /**
     * Returns the heuristic used to determine the next variable to branch on
     * during decision diagram compilation.
     * <p>
     * By default, this method returns a {@link DefaultVariableHeuristic} instance.
     * </p>
     *
     * @return the {@link VariableHeuristic} guiding variable selection
     */
    default VariableHeuristic<T> variableHeuristic() {
        return new DefaultVariableHeuristic<>();
    }
    /**
     * Returns the verbosity level of the solver when this model is executed.
     * <p>
     * By default, the verbosity level is {@link VerbosityLevel#SILENT}.
     * </p>
     *
     * @return the desired {@link VerbosityLevel}
     */
    default VerbosityLevel verbosityLevel() {
        return VerbosityLevel.SILENT;
    }
    /**
     * Returns the debugging level to apply during the compilation and solving phases.
     * <p>
     * By default, debugging is disabled ({@link DebugLevel#OFF}).
     * </p>
     *
     * @return the {@link DebugLevel} controlling debug behavior
     */
    default DebugLevel debugMode() {
        return DebugLevel.OFF;
    }
}
