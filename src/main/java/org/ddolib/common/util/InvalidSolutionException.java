package org.ddolib.common.util;

/**
 * Exception thrown by {@link org.ddolib.layered.modeling.Problem#evaluate(int[])} method if its input solution does not
 * respect the problem's constraints.
 */
public class InvalidSolutionException extends Exception {

    /**
     * Creates a new exception with the given detail message.
     *
     * @param message the detail message describing why the solution is invalid
     */
    public InvalidSolutionException(String message) {
        super(message);
    }
}
