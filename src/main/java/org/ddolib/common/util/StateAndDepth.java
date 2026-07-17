package org.ddolib.common.util;

/**
 * Class containing a state and its depth in the main search.
 *
 * @param state a state of the solved problem
 * @param depth the depth of the input state in the main search
 * @param <T>   the type of the state
 */
public record StateAndDepth<T>(T state, int depth) {

}