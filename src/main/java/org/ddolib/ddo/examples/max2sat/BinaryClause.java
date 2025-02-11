package org.ddolib.ddo.examples.max2sat;

import java.util.Objects;

/**
 * Class to model a Binary clause of two literals for CNF formula. <br>
 * <p>
 * To symbolize a literal <code>a_x</code>, for <code>x >0</code>, we give the value <code>x</code> as input. To
 * symbolize <code> NOT a_x</code>, we give <code>-x</code>.
 */
public class BinaryClause {
    private final int x;
    private final int y;

    public BinaryClause(int x, int y) {
        if (x == 0 || y == 0) throw new IllegalArgumentException("Id of variable in Binary clauses must be != 0");
        this.x = x;
        this.y = y;
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
