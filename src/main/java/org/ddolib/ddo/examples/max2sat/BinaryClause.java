package org.ddolib.ddo.examples.max2sat;

import java.util.Objects;

import static java.lang.Math.abs;

/**
 * Class to model a Binary clause of two literals for CNF formula. <br>
 * <p>
 * To symbolize a literal <code>x_i</code>, for <code>i >0</code>, we give the value <code>i</code> as input. To
 * symbolize <code> NOT x_i</code>, we give <code>-i</code>.
 */
public class BinaryClause {
    public final int i;
    public final int j;

    public BinaryClause(int i, int j) {
        if (i == 0 || j == 0) throw new IllegalArgumentException("Id of variable in Binary clauses must be != 0");
        this.i = i;
        this.j = j;
    }

    /**
     * Evaluates if the clause is verified given 2 boolean values.
     *
     * @param a The value to attribute to the variable <code>x_i</code> (0 or 1).
     * @param b The value to attribute to the variable <code>x_j</code> (0 or 1).
     * @return 0 if the clause is verified. 1 else.
     */
    public int eval(int a, int b) {
        int literal1 = i < 0 ? a ^ 1 : a;
        int literal2 = j < 0 ? b ^ 1 : b;
        return literal1 | literal2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinaryClause other) return this.i == other.i && this.j == other.j;
        else return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(i, j);
    }

    @Override
    public String toString() {
        String notX = i < 0 ? "!" : "";
        String notY = j < 0 ? "!" : "";
        return String.format("%sx_%d || %sx_%d", notX, abs(i), notY, abs(j));
    }
}
