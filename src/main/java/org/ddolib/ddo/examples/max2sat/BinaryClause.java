package org.ddolib.ddo.examples.max2sat;

import java.util.Objects;

/**
 * Class to model a Binary clause of two literals for CNF formula. <br>
 * <p>
 * To symbolize a literal <code>a_x</code>, for <code>x >0</code>, we give the value <code>x</code> as input. To
 * symbolize <code> NOT a_x</code>, we give <code>-x</code>.
 */
public class BinaryClause {
    public final int x;
    public final int y;

    public BinaryClause(int x, int y) {
        if (x == 0 || y == 0) throw new IllegalArgumentException("Id of variable in Binary clauses must be != 0");
        this.x = x;
        this.y = y;
    }

    /**
     * Evaluates if the clause is verified given 2 boolean values.
     *
     * @param a The value to attribute to the variable <code>a_x</code> (0 or 1).
     * @param b The value to attribute to the variable <code>a_y</code> (0 or 1).
     * @return 0 if the clause is verified. 1 else.
     */
    public int eval(int a, int b) {
        int literal1 = x < 0 ? a ^ 1 : a;
        int literal2 = y < 0 ? b ^ 1 : b;
        return literal1 | literal2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinaryClause other) return this.x == other.x && this.y == other.y;
        else return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
