package org.ddolib.ddo.core;

/**
 * This class describes a "decision" in terms of the problem optimization
 */
public final class Decision {
    /**
     * The identifier of the variables
     */
    private final int var;
    /**
     * The value affected to that variable
     */
    private final int value;

    /**
     * Instanciates a decision
     *
     * @param var the variable identifier
     * @param val the value being affected to var
     */
    public Decision(final int var, final int val) {
        this.var = var;
        this.value = val;
    }

    /**
     * The variable involves in the decision
     *
     * @return the identifier of the affected variable
     */
    public int var() {
        return var;
    }

    /**
     * the value involves in the decision
     *
     * @return the value affected to the given variable
     */
    public int val() {
        return value;
    }

    @Override
    public int hashCode() {
        return var * 31 + value;
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof Decision)) {
            return false;
        } else {
            Decision other = (Decision) that;
            return var == other.var && value == other.value;
        }
    }

    @Override
    public String toString() {
        return String.format("Decision: assign %d to var %d", value, var);
    }
}
