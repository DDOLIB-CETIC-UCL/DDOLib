package org.ddolib.ddo.implem.heuristics;

import org.ddolib.ddo.heuristics.WidthHeuristic;

/**
 * This class implements a static maximum width heuristic
 */

/**
 * @param <T> the type of state
 */
public final class FixedWidth<T> implements WidthHeuristic<T> {
    /**
     * The maximum width
     */
    private final int w;

    /**
     * Creates a new instance
     *
     * @param w the fixed max width
     */
    public FixedWidth(final int w) {
        this.w = w;
    }

    @Override
    public int maximumWidth(final T state) {
        return w;
    }
}
