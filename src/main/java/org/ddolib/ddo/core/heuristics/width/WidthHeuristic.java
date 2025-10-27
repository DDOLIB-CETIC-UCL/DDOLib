package org.ddolib.ddo.core.heuristics.width;

/**
 * Interface for heuristics that determine the maximum width of a layer in a multi-valued decision diagram (MDD).
 * <p>
 * Implementations of this interface define how the width of a layer is calculated based on the given state.
 * The maximum width can depend on the state, the depth of the layer, or be a fixed value, depending on the heuristic.
 * </p>
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
}
