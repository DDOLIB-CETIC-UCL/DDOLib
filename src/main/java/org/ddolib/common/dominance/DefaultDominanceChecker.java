package org.ddolib.common.dominance;

import org.ddolib.modeling.Dominance;

/**
 * Default implementation of a {@link DominanceChecker} that performs no dominance checking.
 * <p>
 * This class can be used as a placeholder when dominance pruning is not required or
 * when a problem does not define any dominance relation between states.
 * </p>
 *
 * <p>In decision diagram or search-based algorithms, a dominance checker is used
 * to compare two states and discard those that are dominated (i.e., guaranteed
 * to lead to no better solution). This default implementation disables that feature:
 * it never reports any dominance and never removes any state.</p>
 *
 * @param <T> the type representing the problem state
 * @see DominanceChecker
 * @see Dominance
 */
public class DefaultDominanceChecker<T> extends DominanceChecker<T> {
    /**
     * Constructs a default dominance checker that never declares any state as dominated.
     * <p>
     * Internally, it uses a trivial {@link Dominance} instance that:
     * </p>
     * <ul>
     *   <li>Always returns {@code null} as a key for any state.</li>
     *   <li>Always considers that no state dominates another ({@code false}).</li>
     * </ul>
     */
    public DefaultDominanceChecker() {
        super(new Dominance<>() {
            @Override
            public Object getKey(T state) {
                return null;
            }

            @Override
            public boolean isDominatedOrEqual(T state1, T state2) {
                return false;
            }
        });
    }
    /**
     * Updates the dominance information for a given state.
     * <p>
     * This implementation always returns {@code false}, meaning that
     * no state is ever considered dominated or pruned.
     * </p>
     *
     * @param state    the state being tested for dominance
     * @param depth    the depth (layer) in the search or decision diagram
     * @param objValue the objective value associated with the state
     * @return always {@code false}, since no dominance checking is applied
     */

    @Override
    public boolean updateDominance(T state, int depth, double objValue) {
        return false;
    }

}
