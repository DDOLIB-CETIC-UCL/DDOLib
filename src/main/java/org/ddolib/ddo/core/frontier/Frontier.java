package org.ddolib.ddo.core.frontier;

import org.ddolib.ddo.core.SubProblem;

/**
 * Defines the abstraction of a <b>frontier</b> (or <em>open list</em>) used by solvers
 * to manage and prioritize the remaining subproblems that must be explored.
 * <p>
 * In a Decision Diagram (DD) or branch-and-bound framework, the frontier represents
 * the set of <em>active subproblems</em>—those that have been generated but not yet
 * expanded or solved. Each subproblem corresponds to a node in the search or DD
 * exploration tree.
 * </p>
 *
 * <p>Implementations of this interface define how the solver selects the next node to explore.
 * The typical behavior is to always extract the most promising node (the one with the
 * highest bound on the objective function) to maximize pruning efficiency.</p>
 *
 * <h2>Contract:</h2>
 * <ul>
 *   <li>Nodes (<em>subproblems</em>) are pushed into the frontier using {@link #push(SubProblem)}.</li>
 *   <li>Nodes are extracted in <b>descending order of upper bound value</b> using {@link #pop()}.</li>
 *   <li>Implementations must maintain this priority invariant to ensure correct solver behavior.</li>
 * </ul>
 *
 * @param <T> the type representing the problem state stored in each {@link SubProblem}
 *
 * @see SubProblem
 * @see CutSetType
 */
public interface Frontier<T> {
    /**
     * Adds a new subproblem to the frontier for future exploration.
     * <p>
     * This method inserts a node into the internal priority structure of the frontier
     * (e.g., a priority queue ordered by the subproblem’s upper bound or heuristic value).
     * </p>
     *
     * @param sub the subproblem to be added to the frontier
     */
    void push(final SubProblem<T> sub);

    /**
     * Extracts and returns the most promising subproblem from the frontier.
     * <p>
     * The solver assumes that subproblems are popped in <b>descending order of upper bound</b>,
     * i.e., the node with the best potential objective value should be returned first.
     * </p>
     *
     * <p>If the frontier is empty, this method may return {@code null}.</p>
     *
     * @return the subproblem with the highest priority in the frontier, or {@code null} if empty
     */
    SubProblem<T> pop();

    /**
     * Removes all subproblems currently stored in the frontier.
     * <p>
     * This operation resets the internal structure and is typically used when restarting
     * a search or reinitializing the solver.
     * </p>
     */
    void clear();

    /**
     * Returns the number of subproblems currently stored in the frontier.
     *
     * @return the number of nodes in the frontier
     */
    int size();

    /**
     * Returns the type of cut set associated with this frontier.
     * <p>
     * The cut set type determines the strategy used to define which nodes belong
     * to the frontier (e.g., {@link CutSetType#LastExactLayer} or
     * {@link CutSetType#Frontier}). This affects how the solver manages layers
     * during compilation.
     * </p>
     *
     * @return the {@link CutSetType} representing the frontier strategy
     */
    CutSetType cutSetType();

    /**
     * Checks whether the frontier is empty.
     * <p>
     * This default implementation returns {@code true} if {@link #size()} is zero.
     * </p>
     *
     * @return {@code true} if the frontier contains no subproblems; {@code false} otherwise
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the current <b>best upper bound</b> among all subproblems stored in the frontier.
     * <p>
     * The "best" bound depends on the problem type (e.g., maximum upper bound for minimization
     * problems). This method is generally used by the solver to monitor convergence or
     * update global bounds.
     * </p>
     *
     * <p><b>Note:</b> Implementations should throw an exception if this method is called
     * when the frontier is empty.</p>
     *
     * @return the best upper bound value among subproblems in the frontier
     * @throws IllegalStateException if the frontier is empty
     */
    double bestInFrontier();
}
