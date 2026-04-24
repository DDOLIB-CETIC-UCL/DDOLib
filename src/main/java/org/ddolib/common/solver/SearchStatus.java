package org.ddolib.common.solver;

/**
 * Enumeration representing the possible outcomes of a search process.
 */
public enum SearchStatus {
    /** The search found the optimal solution and proved its optimality. */
    OPTIMAL,
    /** The search proved that no solution exists for the problem. */
    UNSAT,
    /** At least one solution was found, but optimality has not been proven. */
    SAT,
    /** The search status is unknown (e.g., search interrupted or not yet started). */
    UNKNOWN;
}
