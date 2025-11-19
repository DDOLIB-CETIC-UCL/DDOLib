package org.ddolib.ddo.core.heuristics.variable;

import java.util.Iterator;
import java.util.Set;

/**
 * Defines a strategy for selecting the next decision variable to branch on
 * during the construction or exploration of a decision diagram.
 * <p>
 * A {@code VariableHeuristic} is responsible for determining, at each expansion step,
 * which variable should be fixed next among the remaining unassigned ones.
 * It can use information from the current layer’s states to guide this choice.
 * </p>
 *
 * <p>Heuristics of this kind are essential in dynamic programming, search trees,
 * and decision diagrams, as they influence the structure of the diagram and the
 * efficiency of the exploration process. A well-chosen variable ordering can
 * drastically reduce the diagram width and computation time.</p>
 *
 * @param <T> the type representing the problem state
 */
public interface VariableHeuristic<T> {
    /**
     * Selects the next variable to branch on given the current set of
     * unassigned variables and the states of the next layer.
     * <p>
     * The heuristic can analyze the provided states to estimate which variable
     * will best separate or discriminate the search space, improving pruning
     * or convergence. If no meaningful decision can be made, {@code null} may
     * be returned to indicate that the choice should be deferred or decided
     * by a default mechanism.
     * </p>
     *
     * @param variables the set of variable indices that are still unassigned
     * @param states    an iterator over the current states in the next layer
     * @return the index of the next variable to branch on, or {@code null} if
     *         no decision can be made at this point
     */
    Integer nextVariable(final Set<Integer> variables, final Iterator<T> states);
}
