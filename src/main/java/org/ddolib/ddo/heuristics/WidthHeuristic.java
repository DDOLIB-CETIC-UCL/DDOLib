package org.ddolib.ddo.heuristics;

/**
 * This heuristic is used to determine the maximum width of a layer
 * in an MDD which is compiled using a given state as root.
 */
public interface WidthHeuristic<T> {
    int maximumWidth(final T state);
}
