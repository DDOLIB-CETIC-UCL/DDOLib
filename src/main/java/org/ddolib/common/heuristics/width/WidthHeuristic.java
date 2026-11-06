package org.ddolib.common.heuristics.width;

/**
 * Interface for heuristics that determine the maximum width of a layer in a multi-valued decision
 * diagram (MDD).
 *
 * <p>Implementations of this interface define how the width of a layer is calculated based on the
 * given state. The maximum width can depend on the state, the depth of the layer, or be a fixed
 * value, depending on the heuristic.
 *
 * @param <T> the type of state used to compute the layer width
 */
public interface WidthHeuristic<T> {
    /**
     * Computes the maximum width allowed for a layer rooted at the given state.
     *
     * @param state the state at the root of the layer
     * @return the maximum width for the layer
     */
    int maximumWidth(final T state);

    /**
     * Computes the maximum width allowed for the decision diagrams compiled from a subproblem,
     * given its state and its depth (the number of variables already assigned). The solvers call
     * this method; by default, it ignores the depth and returns {@link #maximumWidth(Object)}.
     *
     * @param state the state of the subproblem
     * @param depth the depth of the subproblem, i.e. its number of assigned variables
     * @return the maximum width for the layers of the decision diagrams
     */
    default int maximumWidth(final T state, final int depth) {
        return maximumWidth(state);
    }
}
