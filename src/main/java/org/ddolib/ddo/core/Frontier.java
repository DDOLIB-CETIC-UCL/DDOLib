package org.ddolib.ddo.core;

/**
 * An abstraction of the solver frontier that maintains all the remaining open
 * subproblems that must be solved.
 * @param <T> the type of state
 */
public interface Frontier<T> {
    /**
     * This is how you push a node onto the frontier.
     *
     * @param sub the subproblem you want to push onto the frontier
     */
    void push(final SubProblem<T> sub);

    /**
     * This method yields the most promising node from the frontier.
     * <p>
     * # Note:
     * The solvers rely on the assumption that a frontier will pop nodes in
     * descending upper bound order. Hence, it is a requirement for any fringe
     * implementation to enforce that requirement.
     *
     * @return the most promising sub problem from the frontier (or null if the frontier is empty)
     */
    SubProblem<T> pop();

    /**
     * This method clears the frontier: it removes all nodes from the queue.
     */
    void clear();

    /**
     * @return the length of the queue.
     */
    int size();

    /**
     * @return the type of the frontier
     */
    CutSetType cutSetType();

    /**
     * @return true iff the frontier is empty (size == 0)
     */
    default boolean isEmpty() {
        return size() == 0;
    }

}
