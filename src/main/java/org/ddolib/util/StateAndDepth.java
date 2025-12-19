package org.ddolib.util;

/**
 * Class containing a state and its depth in the main search.
 *
 * @param state A state of the solved problem.
 * @param depth The depth of the input state in the main search.
 * @param <T>   The type of the state.
 */
public record StateAndDepth<T>(T state, int depth) {

}