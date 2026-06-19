package org.ddolib.modeling.nolayer;

import org.ddolib.modeling.InvalidSolutionException;

import java.util.Iterator;
import java.util.Optional;

/**
 * Represents an optimization problem formulated as a labeled transition
 * system, without requiring an a priori fixed number of variables.
 *
 * @param <T> the type representing a state in the problem
 */
public interface Problem<T> {

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
     * Checks whether the given state is a target (sink) state.
     *
     * @param state the current state
     * @return true if the state is a target state, false otherwise
     */
    boolean isTarget(final T state);

    /**
     * Returns the domain of possible labels/actions from a given state.
     *
     * @param state the current state
     * @return an iterator over all feasible labels for the given state
     */
    Iterator<Integer> domain(final T state);

    /**
     * Applies a label/action to a state, computing the next state according
     * to the problem's transition function.
     *
     * @param state the state from which the transition originates
     * @param label the label to apply
     * @return the resulting state after applying the label
     */
    T transition(final T state, final int label);

    /**
     * Computes the change in objective value resulting from applying
     * a label to a given state.
     *
     * @param state the state from which the transition originates
     * @param label the label to apply
     * @return the incremental objective cost/value associated with this label
     */
    double transitionCost(final T state, final int label);

    /**
     * Returns the known optimal value of the problem, if available.
     *
     * @return an {@code Optional} containing the known optimal value, or empty if unknown
     */
    default Optional<Double> optimalValue() {
        return Optional.empty();
    }

    /**
     * Given a solution (a sequence of applied labels), returns its value and checks
     * if the solution respects the problem's constraints.
     *
     * @param solution A solution of the problem (sequence of labels).
     * @return The value of the input solution
     * @throws InvalidSolutionException If the solution does not respect problem's constraints.
     */
    double evaluate(final int[] solution) throws InvalidSolutionException;
}
