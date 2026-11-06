package org.ddolib.common.solver.stopcriterion;

/**
 * Signals that a search reached its memory limit, see {@link StopCriterion#maxFrontierSize(int)}.
 * The solvers catch it, like an {@link OutOfMemoryError}, and return the best solution found so
 * far with the status {@link org.ddolib.common.solver.stat.SearchStatus#MEMOUT}.
 */
public class MemoryLimitException extends RuntimeException {

    /**
     * Creates the exception. No stack trace is recorded, it is not needed to stop the search.
     *
     * @param message the description of the limit that was reached
     */
    public MemoryLimitException(String message) {
        super(message, null, false, false);
    }
}
