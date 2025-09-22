package org.ddolib.ddo.core;

import java.util.Set;

/**
 * A subproblem is a residual problem that must be solved in order to complete the
 * resolution of the original problem which had been defined.
 * <p>
 * Subproblems are instantiated from nodes in the exact custsets of relaxed decision
 * diagrams.
 *
 * @param <T> the type of state
 */
public final class SubProblem<T> {
    /**
     * The root state of this subproblem
     */
    final T state;
    /**
     * The root value of this subproblem
     */
    final double value;
    /**
     * A lower bound on the objective reachable in this subproblem
     */
    final double lb;
    /**
     * The path to traverse to reach this subproblem from the root of the original
     * problem
     */
    final Set<Decision> path;

    /**
     * Creates a new subproblem instance
     *
     * @param state the root state of this subproblem
     * @param value the value of the longest path to this subproblem
     * @param lb    a lower bound on the optimal value reachable when solving the global
     *              problem through this subproblem
     * @param path  the partial assignment leading to this subproblem from the root
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
     * @return the depth of the root of this subproblem
     */
    public int getDepth() {
        return this.path.size();
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
    public double getValue() {
        return this.value;
    }

    /**
     * @return a lower bound on the global objective if solved using this subproblem
     */
    public double getLowerBound() {
        return this.lb;
    }

    /**
     * @return the f-value of this subproblem, i.e., the value of the longest path to this
     * subproblem plus the upper bound on the objective reachable in this subproblem
     */
    public double f() {
        return this.value + this.lb;
    }

    /**
     * @return the path (partial assignment) which led to this very node
     */
    public Set<Decision> getPath() {
        return this.path;
    }

    public String statistics() {
        return String.format("SubProblem(val:%.0f ub:%.0f fub:%.0f depth:%d)", value, lb, (value - lb), this.getPath().size());
    }

    @Override
    public String toString() {
        return String.format("SubProblem(val: %.0f - lb: %.0f - f: %.0f - depth: %d - state: %s",
                value, lb, f(), this.getPath().size(), state);
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
