package org.ddolib.modeling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.examples.ddo.carseq.CSProblem;
import org.ddolib.examples.ddo.carseq.CSRelax;

/**
 * Mapping between an initial problem and an aggregated problem.
 * Allows mapping a decision in the initial problem to a decision in the aggregated problem.
 * It also provides the required classes for solving the aggregated problem (problem, relaxation, fast upper bound, ...)
 */
public interface Aggregate<T> {
    /**
     * Get the aggregated problem
     */
    Problem<T> getProblem();

    /**
     * Get the relaxation operator for the aggregated problem
     */
    Relaxation<T> getRelax();

    /**
     * Map a decision
     * @param decision Decision in the initial problem
     * @return Decision in the aggregated problem
     */
    Decision mapDecision(Decision decision);
}
