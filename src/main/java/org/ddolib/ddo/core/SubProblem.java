package org.ddolib.ddo.core;

import java.util.Set;

/**
 * A subproblem is a residual problem that must be solved in order to complete the
 * resolution of the original problem which had been defined.
 * <p>
 * Subproblems are instantiated from nodes in the exact custsets of relaxed decision
 * diagrams
 */
public final class SubProblem<T> {
    /**
     * The root state of this sub problem
     */
    final T state;
    /**
     * The root value of this sub problem
     */
    final int value;
    /**
     * An upper bound on the objective reachable in this subproblem
     */
    final int ub;
    /**
     * The path to traverse to reach this subproblem from the root of the original
     * problem
     */
    final Set<Decision> path;

    /**
     * Creates a new subproblem instance
     *
     * @param state the root state of this sub problem
     * @param value the value of the longest path to this subproblem
     * @param ub    an upper bound on the optimal value reachable when solving the global
     *              problem through this sub problem
     * @param path  the partial assignment leading to this subproblem from the root
     */
    public SubProblem(
            final T state,
            final int value,
            final int ub,
            final Set<Decision> path) {
        this.state = state;
        this.value = value;
        this.ub = ub;
        this.path = path;
    }

    /**
     * @return the root state of this subproblem
     */
    public T getState() {
        return this.state;
    }

    /**
     * @return the objective value at the root of this subproblem
     */
    public int getValue() {
        return this.value;
    }

    /**
     * @return an upper bound on the global objective if solved using this subproblem
     */
    public int getUpperBound() {
        return this.ub;
    }

    /**
     * @return the path (partial assignment) which led to this very node
     */
    public Set<Decision> getPath() {
        return this.path;
    }

    @Override
    public String toString() {
        return String.format("Subproblem: value: %d - ub: %d - state: %s", value, ub, state);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SubProblem<?> other) {
            return this.state.equals(other.state);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.state.hashCode();
    }
}
