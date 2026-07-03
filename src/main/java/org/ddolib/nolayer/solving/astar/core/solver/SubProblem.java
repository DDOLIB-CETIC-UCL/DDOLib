package org.ddolib.nolayer.solving.astar.core.solver;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A subproblem used in the AStarSolver.
 * Represents a node in the A* search tree.
 *
 * @param <T> the type of the state
 */
public class SubProblem<T> {
    private final T state;
    private final double g; // path cost
    private final double h; // heuristic cost to target
    private final List<Integer> path; // sequence of labels applied

    public SubProblem(T state, double g, double h, List<Integer> path) {
        this.state = state;
        this.g = g;
        this.h = h;
        this.path = path;
    }

    public T getState() {
        return state;
    }

    public double getValue() {
        return g;
    }

    public double getLowerBound() {
        return h;
    }

    public double f() {
        return g + h;
    }

    public List<Integer> getPath() {
        return Collections.unmodifiableList(path);
    }

    @Override
    public String toString() {
        return "%s - g: %f - h: %f".formatted(state, g, h);
    }

    /**
     * Two subproblems are considered equal if they share the same state. Solvers rely on this
     * to keep at most one entry per state in their priority queues (e.g. when a better path to
     * an already open state replaces the stale entry).
     *
     * @param obj the object to compare with
     * @return {@code true} if both subproblems have the same state, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SubProblem<?> other) {
            return Objects.equals(this.state, other.state);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(state);
    }
}
