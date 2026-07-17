package org.ddolib.layered.modeling;

/**
 * Object that, given a dominance, will check if a state is dominated.
 *
 * @param <T> the type of states
 */
public abstract class DominanceChecker<T> {

    /**
     * The dominance relation used to compare states.
     */
    protected final Dominance<T> dominance;

    /**
     * Creates a new dominance checker relying on the given dominance relation.
     *
     * @param dominance the dominance relation used to compare states
     */
    protected DominanceChecker(Dominance<T> dominance) {
        this.dominance = dominance;
    }

    /**
     * Checks whether the input state is dominated and updates the front of non-dominated nodes.
     *
     * @param state    the state on which test dominance
     * @param depth    the depth of the state in the MDD
     * @param objValue the length of the longest path from the root to the input state
     * @return whether the input state is dominated
     */
    public abstract boolean updateDominance(T state, int depth, double objValue);

    /**
     * Clears the dominance checker state.
     * This is useful for search algorithms that restart and need to clear previously cached dominance relations.
     */
    public void clear() {
        // Default implementation does nothing.
    }
}
