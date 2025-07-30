package org.ddolib.modeling;

import org.ddolib.ddo.core.Decision;

import java.util.Set;

/**
 * Mapping between an initial problem and an aggregated problem.
 * Allows mapping a transition in the initial problem to a transition in the aggregated problem.
 * It also provides the required classes for solving the aggregated problem (problem, relaxation, fast upper bound, ...)
 */
public interface Aggregate<T, K> {
    /**
     * Get the aggregated problem with all required classes (relaxation, fast upper bound, ...)
     */
    SolverInput<T, K> getProblem();

    /**
     * Applies a transition in the aggregated problem corresponding to a decision in the initial problem.
     * @param state Current aggregated state
     * @param decision Decision in the initial problem
     * @return Next aggregated state
     */
    T aggregateTransition(T state, Decision decision);

    /**
     * Returns the variable that is assigned in the aggregated problem when
     * applying a transition corresponding to a decision in the initial problem.
     * If no variable was assigned (this is possible for some problems, for example
     * if the number of variables is different in both problems), returns -1.
     * <p>
     * # Note:
     * It is assumed that the variables are assigned in the same order when exploring
     * the aggregated problem alone (using the variable heuristic) and when exploring
     * it with the associated initial problem (using this method).
     * @param state Current aggregated state
     * @param decision Decision in the initial problem
     * @param variables The set of unassigned variables in the aggregated problem
     * @return Index of the assigned variable, or -1 if no variable was assigned
     */
    int assignedVariable(T state, Decision decision, Set<Integer> variables);
}