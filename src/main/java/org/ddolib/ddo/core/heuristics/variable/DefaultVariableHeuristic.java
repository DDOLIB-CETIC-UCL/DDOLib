package org.ddolib.ddo.core.heuristics.variable;


import org.ddolib.modeling.Problem;

import java.util.Iterator;
import java.util.Set;

/**
 * A default implementation of {@link VariableHeuristic} that selects
 * the next variable to branch on without applying any specific strategy.
 * <p>
 * This heuristic simply returns the first element obtained from the iterator
 * of the provided {@code variables} set. Therefore, it does not guarantee
 * any particular or deterministic order, as the iteration order of a
 * {@link java.util.Set} depends on its concrete implementation
 * (e.g., {@link java.util.HashSet} is not ordered).
 * </p>
 *
 * <p>This class serves as a minimal or placeholder heuristic to ensure
 * that the compilation or search process can proceed when no specific
 * variable ordering rule has been defined by the user.</p>
 *
 * @param <T> the type representing the problem state
 * @see VariableHeuristic
 * @see java.util.Set
 */
public final class DefaultVariableHeuristic<T> implements VariableHeuristic<T> {
    /**
     * Returns the next variable to branch on by selecting the first element
     * from the set of remaining variables.
     * <p>
     * This implementation applies no logic based on the current layer states
     * and provides no guarantees on reproducibility unless a deterministic
     * {@link java.util.Set} implementation (e.g., {@link java.util.LinkedHashSet})
     * is used.
     * </p>
     *
     * @param variables the set of variables that have not yet been assigned
     * @param states    an iterator over the states in the current layer (unused)
     * @return the next variable to branch on, as given by {@code variables.iterator().next()}
     * @throws java.util.NoSuchElementException if the {@code variables} set is empty
     */
    @Override
    public Integer nextVariable(final Set<Integer> variables, final Iterator<T> states) {
        return variables.iterator().next();
    }

}
