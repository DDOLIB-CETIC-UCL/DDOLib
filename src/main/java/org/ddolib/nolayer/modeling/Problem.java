package org.ddolib.nolayer.modeling;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.ddolib.common.util.InvalidSolutionException;

/**
 * Represents an optimization problem formulated as a labeled transition system, without requiring
 * an a priori fixed number of variables.
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
     * Applies a label/action to a state, computing the next state according to the problem's
     * transition function.
     *
     * @param state the state from which the transition originates
     * @param label the label to apply
     * @return the resulting state after applying the label
     */
    T transition(final T state, final int label);

    /**
     * Computes the change in objective value resulting from applying a label to a given state.
     *
     * @param state the state from which the transition originates
     * @param label the label to apply
     * @return the incremental objective cost/value associated with this label
     */
    double transitionCost(final T state, final int label);

    /**
     * Indicates whether all the transition costs are non-negative, i.e. whether the cost of a
     * partial solution can only increase when it is completed.
     *
     * <p>It is only used when the model does not provide a {@link FastLowerBound}: the solvers
     * that report a lower bound and a gap (A*, AWA*, ACS) then use the cost of the best open node
     * as lower bound, which is only valid when the remaining costs cannot be negative. With the
     * default value {@code false}, no lower bound is reported in that case (the gap is infinite).
     * Maximization problems modeled with negated profits have negative costs and must keep the
     * default value.
     *
     * @return {@code true} if every transition cost is non-negative
     */
    default boolean hasNonNegativeCosts() {
        return false;
    }

    /**
     * Returns the known optimal value of the problem, if available.
     *
     * @return an {@code Optional} containing the known optimal value, or empty if unknown
     */
    default Optional<Double> optimalValue() {
        return Optional.empty();
    }

    /**
     * Given a solution (a sequence of applied labels), returns its value and checks if the solution
     * respects the problem's constraints.
     *
     * @param solution a solution of the problem (sequence of labels)
     * @return the value of the input solution
     * @throws InvalidSolutionException if the solution does not respect problem's constraints
     */
    double evaluate(final List<Integer> solution) throws InvalidSolutionException;
}
