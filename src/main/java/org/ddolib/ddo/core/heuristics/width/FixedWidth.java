package org.ddolib.ddo.core.heuristics.width;


/**
 * Implements a static maximum width heuristic for decision diagram or search-based algorithms.
 * <p>
 * This heuristic always returns a fixed maximum width for any state, regardless of the depth or
 * characteristics of the state. It can be used to restrict the size of a decision diagram (DD)
 * or a layer in a search algorithm.
 * </p>
 *
 * @param <T> the type of state for which the width is calculated
 */
public final class FixedWidth<T> implements WidthHeuristic<T> {
    /** The fixed maximum width. */
    private final int w;

    /**
     * Constructs a new {@code FixedWidth} heuristic with the specified maximum width.
     *
     * @param w the fixed maximum width
     */
    public FixedWidth(final int w) {
        this.w = w;
    }
    /**
     * Returns the maximum width for the given state.
     * <p>
     * Since this is a fixed-width heuristic, the same value {@code w} is returned for every state.
     * </p>
     *
     * @param state the state (ignored in this heuristic)
     * @return the fixed maximum width
     */
    @Override
    public int maximumWidth(final T state) {
        return w;
    }
}
