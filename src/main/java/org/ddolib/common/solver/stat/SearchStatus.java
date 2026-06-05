package org.ddolib.common.solver.stat;

/**
 * Status values describing the state of a search process.
 */
public enum SearchStatus {
    /**
     * The search found an optimal solution and proved its optimality.
     */
    OPTIMAL,
    /**
     * The search proved that no solution exists.
     */
    UNSAT,
    /**
     * The search found a feasible solution but did not prove its optimality.
     */
    SAT,
    /**
     * The search status is unknown (e.g., interrupted before finding any solution).
     */
    UNKNOWN;
}