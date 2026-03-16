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
 * System.out.println(d); // prints: Decision(2, 5)
 * //Where 2 is the id of the varible and 5 the assigned value
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
    private final int _variable;
    /**
     * The value assigned to the variable.
     */
    private final int _value;

    /**
     * Constructs a new {@code Decision} representing the assignment of a value
     * to a variable.
     *
     * @param variable the identifier of the variable
     * @param value    the value assigned to that variable
     */
    public Decision(final int variable, final int value) {
        this._variable = variable;
        this._value = value;
    }

    /**
     * Returns the identifier of the variable involved in this decision.
     *
     * @return the variable identifier
     */
    public int var() {
        return _variable;
    }

    /**
     * Returns the value assigned to the variable in this decision.
     *
     * @return the assigned value
     */
    public int val() {
        return _value;
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     * This ensures decisions can be efficiently used in hash-based collections.
     *
     * @return a hash code value for this decision
     */
    @Override
    public int hashCode() {
        return _variable * 31 + _value;
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
        if (that instanceof Decision other) {
            return _variable == other._variable && _value == other._value;
        } else {
            return false;
        }
    }

    /**
     * Returns a human-readable representation of this decision.
     *
     * @return a formatted string describing the variable assignment
     */
    @Override
    public String toString() {
        return String.format("Decision(%d, %d)", _variable, _value);
    }
}
