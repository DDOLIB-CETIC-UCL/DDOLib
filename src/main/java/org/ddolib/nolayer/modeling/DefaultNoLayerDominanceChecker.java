package org.ddolib.nolayer.modeling;

/**
 * Default implementation of a {@link NoLayerDominanceChecker} that performs no dominance
 * checking.
 * <p>
 * This class can be used as a placeholder when dominance pruning is not required or
 * when a problem does not define any dominance relation between states: it never reports
 * any state as dominated.
 *
 * @param <T> the type representing the problem state
 */
public class DefaultNoLayerDominanceChecker<T> implements NoLayerDominanceChecker<T> {

    /**
     * Creates a new instance of this default dominance checker.
     */
    public DefaultNoLayerDominanceChecker() {
    }

    /**
     * Updates the dominance information for a given state.
     * <p>
     * This implementation always returns {@code false}, meaning that no state is ever
     * considered dominated or pruned.
     *
     * @param state the state being tested for dominance
     * @param value the objective value associated with the state
     * @return always {@code false}, since no dominance checking is applied
     */
    @Override
    public boolean updateDominance(T state, double value) {
        return false;
    }
}
