package org.ddolib.astar.core.solver.nolayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
}
