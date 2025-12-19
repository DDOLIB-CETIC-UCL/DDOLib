package org.ddolib.ddo.core;

/**
 * Represents a single decision within an optimization problem.
 * <p>
 * A {@code Decision} associates a variable identifier with a specific value.
 * It is an immutable data structure that captures a single assignment performed
 * during the search or compilation of a decision diagram (e.g., in an MDD-based solver).
 * </p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * Decision d = new Decision(2, 5);
 * System.out.println(d); // prints: Decision: assign 5 to var 2
 * }</pre>
 *
 * <p><b>Use in DD-based solvers:</b></p>
 * <ul>
 *   <li>Encapsulates the assignment of a value to a variable during branching.</li>
 *   <li>Used to reconstruct solutions from paths in the decision diagram.</li>
 *   <li>Acts as a key element in comparing or storing decision paths in sets and maps.</li>
 * </ul>
 *
 * <p>This class is immutable and provides consistent implementations of
 * {@link #equals(Object)} and {@link #hashCode()} to allow its use as a key
 * in collections such as {@link java.util.HashSet} or {@link java.util.HashMap}.
 * </p>
 */
public final class Decision {
    /**
     * Identifier of the variable involved in this decision.
     */
    private final int var;
    /**
     * The value assigned to the variable.
     */
    private final int value;

    /**
     * Constructs a new {@code Decision} representing the assignment of a value
     * to a variable.
     *
     * @param var the identifier of the variable
     * @param val the value assigned to that variable
     */
    public Decision(final int var, final int val) {
        this.var = var;
        this.value = val;
    }

    /**
     * Returns the identifier of the variable involved in this decision.
     *
     * @return the variable identifier
     */
    public int var() {
        return var;
    }

    /**
     * Returns the value assigned to the variable in this decision.
     *
     * @return the assigned value
     */
    public int val() {
        return value;
    }
    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     * This ensures decisions can be efficiently used in hash-based collections.
     *
     * @return a hash code value for this decision
     */
    @Override
    public int hashCode() {
        return var * 31 + value;
    }
    /**
     * Compares this decision to another object for equality.
     * Two decisions are equal if they assign the same value to the same variable.
     *
     * @param that the object to compare with
     * @return {@code true} if both decisions are identical, {@code false} otherwise
     */
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof Decision)) {
            return false;
        } else {
            Decision other = (Decision) that;
            return var == other.var && value == other.value;
        }
    }
    /**
     * Returns a human-readable representation of this decision.
     *
     * @return a formatted string describing the variable assignment
     */
    @Override
    public String toString() {
        return String.format("Decision: assign %d to var %d", value, var);
    }
}
