package org.ddolib.util;

/**
 * Exception thrown by {@link org.ddolib.layered.modeling.Problem#evaluate(int[])} method if its input solution does not
 * respect the problem's constraints.
 */
public class InvalidSolutionException extends Exception {
    public InvalidSolutionException(String message) {
        super(message);
    }
}
