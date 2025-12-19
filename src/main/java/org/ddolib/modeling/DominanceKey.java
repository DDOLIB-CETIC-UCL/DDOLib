package org.ddolib.modeling;
/**
 * Defines a function that extracts a canonical dominance key from a given state.
 * <p>
 * A {@code DominanceKey} serves as a mapping between problem states and a
 * representative key used to group equivalent or comparable states under
 * a dominance relation. This abstraction allows the solver to efficiently
 * detect and manage duplicate or dominated states when exploring a search
 * space or compiling a decision diagram.
 * </p>
 *
 * <p>
 * The extracted key can represent structural or semantic properties of the state
 * that are relevant to the dominance relation. Two states producing the same
 * key are typically considered to belong to the same equivalence class for
 * dominance checking.
 * </p>
 *
 * @param <T> the type representing the problem state
 * @param <K> the type of the extracted dominance key
 */
public interface DominanceKey<T, K> {
    /**
     * Returns the dominance key associated with the given state.
     * <p>
     * Implementations should extract and return a value that uniquely or canonically
     * represents the relevant features of the state for dominance comparison.
     * States that share the same key will be compared using the associated
     * {@link Dominance} relation to determine whether one dominates the other.
     * </p>
     *
     * @param state the state from which to extract the dominance key
     * @return the canonical key corresponding to the given state
     */
    K value(T state);

}
