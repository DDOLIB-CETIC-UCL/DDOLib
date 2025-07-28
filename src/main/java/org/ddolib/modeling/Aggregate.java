package org.ddolib.modeling;

import org.ddolib.ddo.core.Decision;

/**
 * Mapping between an initial problem and an aggregated problem.
 * Allows mapping a decision in the initial problem to a decision in the aggregated problem.
 * It also provides the required classes for solving the aggregated problem (problem, relaxation, fast upper bound, ...)
 */
public interface Aggregate<T, K> {
    /**
     * Get the aggregated problem with all required classes (relaxation, fast upper bound, ...)
     */
    SolverInput<T, K> getProblem();

    /**
     * Map a decision
     * @param decision Decision in the initial problem
     * @return Decision in the aggregated problem
     */
    Decision mapDecision(Decision decision);
}