package org.ddolib.ddo.core;

import java.util.Objects;
import java.util.Set;

/**
 * Represents a residual optimization problem (subproblem) derived from the decomposition
 * of an original problem during the compilation or search process.
 * <p>
 * A {@code SubProblem} corresponds to a portion of the global problem that must be solved
 * in order to complete the resolution of the original optimization task. Subproblems are
 * typically created from nodes located in the exact cutsets of relaxed decision diagrams
 * and encapsulate the remaining computation required from that point onward.
 * </p>
 *
 * <p>
 * Each subproblem maintains:
 * </p>
 * <ul>
 *   <li>The current {@link #state} of the problem at its root,</li>
 *   <li>The cumulative objective {@link #value} up to that state,</li>
 *   <li>A lower bound {@link #lb} estimating the best achievable value below it, and</li>
 *   <li>The {@link #path} of {@link Decision decisions} taken to reach this subproblem.</li>
 * </ul>
 * These attributes allow the solver to estimate, compare, and prioritize subproblems when
 * exploring the decision space.
 *
 * @param <T> the type representing the state of the problem
 */
public final class SubProblem<T> {
    /**
     * The root state associated with this subproblem.
     * Represents the current configuration of the problem.
     */
    final T state;
    /**
     * The objective value accumulated up to the root of this subproblem.
     */
    final double value;
    /**
     * A lower bound on the optimal objective value reachable
     * when continuing from this subproblem.
     */
    final double lb;
    /**
     * The sequence of {@link Decision decisions} made from the root of the original problem
     * to reach this subproblem. Represents the partial assignment of variables.
     */
    final Set<Decision> path;

    /**
     * Constructs a new {@code SubProblem} instance with its associated state,
     * accumulated value, lower bound, and decision path.
     *
     * @param state the root state of this subproblem
     * @param value the objective value corresponding to the longest path reaching this subproblem
     * @param lb    a lower bound on the optimal value reachable when solving through this subproblem
     * @param path  the sequence of decisions (partial assignment) leading from the root to this subproblem
     */
    public SubProblem(
            final T state,
            final double value,
            final double lb,
            final Set<Decision> path) {
        this.state = state;
        this.value = value;
        this.lb = lb;
        this.path = path;
    }

    /**
     * Returns the depth of this subproblem, corresponding to the number of
     * decisions taken from the root to reach it.
     *
     * @return the depth of the subproblem
     */
    public int getDepth() {
        return this.path.size();
    }

    /**
     * Returns the root state associated with this subproblem.
     *
     * @return the root state
     */
    public T getState() {
        return this.state;
    }

    /**
     * Returns the cumulative objective value at the root of this subproblem.
     *
     * @return the current objective value
     */
    public double getValue() {
        return this.value;
    }

    /**
     * Returns the lower bound on the global objective that can be achieved
     * by solving the remainder of the problem through this subproblem.
     *
     * @return the lower bound on the objective
     */
    public double getLowerBound() {
        return this.lb;
    }

    /**
     * Computes and returns the <em>f-value</em> of this subproblem, defined as
     * the sum of its current objective value and its lower bound.
     * <p>
     * This metric is often used to prioritize subproblems in branch-and-bound
     * or decision diagram exploration.
     * </p>
     *
     * @return the f-value (value + lower bound) of this subproblem
     */
    public double f() {
        return this.value + this.lb;
    }

    /**
     * Returns the path (partial assignment) of decisions that led to this subproblem.
     *
     * @return the set of decisions representing the path
     */
    public Set<Decision> getPath() {
        return this.path;
    }

    /**
     * Returns a string summarizing key statistics about this subproblem,
     * including its objective value, lower bound, and depth in the decision diagram.
     *
     * @return a formatted string of subproblem statistics
     */
    public String statistics() {
        return String.format("SubProblem(val:%.0f ub:%.0f fub:%.0f depth:%d)", value, lb, (value - lb), this.getPath().size());
    }

    /**
     * Returns the hash code of this subproblem, derived from its root state.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(state, getDepth());
    }

    /**
     * Compares this subproblem to another for equality.
     * Two subproblems are considered equal if they share the same root state.
     *
     * @param obj the object to compare with
     * @return {@code true} if both subproblems have the same state, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SubProblem<?> other) {
            return this.state.equals(other.state) && this.getDepth() == other.getDepth();
        }
        return false;
    }

    /**
     * Returns a human-readable representation of this subproblem,
     * including its value, lower bound, f-value, depth, and state.
     *
     * @return a formatted string describing this subproblem
     */
    @Override
    public String toString() {
        return String.format("SubProblem(val: %.0f - lb: %.0f - f: %.0f - depth: %d - state: %s",
                value, lb, f(), this.getPath().size(), state);
    }

}
