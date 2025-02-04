package org.ddolib.ddo.examples.max2sat;

/**
 * Class to model a Binary clause of two literals: (x || y), ( !x || y), (x || !y), (!x || !y).
 * To model !x, the value x must < 0
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
        if (obj instanceof BinaryClause bc) return this.x == bc.x && this.y == bc.y;
        else return false;
    }
}
