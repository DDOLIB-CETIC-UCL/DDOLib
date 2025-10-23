package org.ddolib.modeling;

import org.ddolib.ddo.core.Decision;

import java.util.Iterator;
import java.util.Optional;
import org.ddolib.ddo.core.solver.ExactSolver;
import org.ddolib.ddo.core.solver.SequentialSolver;

/**
 * Represents an optimization problem formulated as a labeled transition
 * system, following the semantics of dynamic programming.
 * <p>
 * A {@code Problem} defines the state space, the transitions between states
 * induced by decisions, and the objective values associated with those transitions.
 * Implementations provide the essential operations required by solvers such as
 * {@link SequentialSolver} or {@link ExactSolver}.
 *
 * @param <T> the type representing a state in the problem
 */
public interface Problem<T> {
    /**
     * @return the total number of decision variables in this problem
     */
    int nbVars();

    /**
     * Returns the initial state of the problem.
     *
     * @return the state representing the starting point of the optimization
     */
    T initialState();

    /**
     * Returns the initial objective value associated with the initial state.
     *
     * @return the starting value of the objective function
     */
    double initialValue();

    /**
     * Returns the domain of possible values for a given variable
     * when applied to a specific state.
     *
     * @param state the current state
     * @param var   the variable index whose domain is queried
     * @return an iterator over all feasible values for the variable in this state
     */
    Iterator<Integer> domain(final T state, final int var);

    /**
     * Applies a decision to a state, computing the next state according
     * to the problem's transition function.
     *
     * @param state    the state from which the transition originates
     * @param decision the decision to apply
     * @return the resulting state after applying the decision
     */
    T transition(final T state, final Decision decision);

    /**
     * Computes the change in objective value resulting from applying
     * a decision to a given state.
     *
     * @param state    the state from which the transition originates
     * @param decision the decision to apply
     * @return the incremental objective cost/value associated with this decision
     */
    double transitionCost(final T state, final Decision decision);

    /**
     * Returns the known optimal value of the problem, if available.
     * <p>
     * <b>Note:</b> This value should correspond to the expected output
     * of the solver. For minimization problems, be careful with negative values.
     *
     * @return an {@code Optional} containing the known optimal value, or empty if unknown
     */
    default Optional<Double> optimalValue() {
        return Optional.empty();
    }
}
