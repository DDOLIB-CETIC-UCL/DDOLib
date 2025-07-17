package org.ddolib.ddo.core.heuristics.width;

/**
 * This heuristic is used to determine the maximum width of a layer
 * in an MDD which is compiled using a given state as root.
 *
 * @param <T> the type of state
 */
public interface WidthHeuristic<T> {
    int maximumWidth(final T state);
}
