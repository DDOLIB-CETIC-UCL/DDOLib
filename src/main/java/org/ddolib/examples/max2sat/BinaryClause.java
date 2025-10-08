package org.ddolib.examples.max2sat;

import java.util.Objects;

import static java.lang.Math.abs;

/**
 * Class to model a Binary clause of two literals for CNF formula. <br>
 * <p>
 * To symbolize a literal {@code x_i    }, for {@code i > 0}, we give the value {@code i} as
 * input. To symbolize {@code NOT x_i}, we give {@code -i}.
 */
public class BinaryClause implements Comparable<BinaryClause> {
    public final int i;
    public final int j;

    public BinaryClause(int i, int j) {
        if (i == 0 || j == 0)
            throw new IllegalArgumentException("Id of variable in Binary clauses must be != 0");
        this.i = i;
        this.j = j;
    }

    /**
     * Evaluates if the clause is verified given 2 boolean values.
     *
     * @param a The value to attribute to the variable {@code x_i} (0 or 1).
     * @param b The value to attribute to the variable {@code x_j} (0 or 1).
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

    private int internalCompare(int x, int y) {
        int compared = Integer.compare(abs(x), abs(y));
        if (compared == 0) {
            return -Integer.compare(x, y);
        } else {
            return compared;
        }
    }

    /**
     * Used to compare binary clauses. It is used to sort them when generating Max2Sat instances.
     * <br>
     * The induced order is the following:
     * <ol>
     *     <li>The lexical order on the literals' indices. </li>
     *     <li>The positive literal before the negative ones.</li>
     * </ol>
     *
     * @param other The binary clause to be compared.
     * @return <ul>
     *     <li> {@code 0} if if {@code this == other}</li>
     *     <li><code>1</code> if {@code this > other}</li>
     *     <li><code>-1</code> if {@code this < other}</li>
     * </ul>
     */
    @Override
    public int compareTo(BinaryClause other) {
        int lit1Compare = internalCompare(this.i, other.i);
        if (lit1Compare == 0) {
            return internalCompare(this.j, other.j);
        } else {
            return lit1Compare;
        }
    }
}
