package org.ddolib.astar.examples.JobShop;

/**
 * Implementation of a Precedence
 * i ->j ; i precedes j
 */
public class Precedence {
    public int i;
    public int j;

    public Precedence(int i, int j) {
        this.i = i;
        this.j = j;
    }
    @Override
    public String toString() {
        return i+" ->"+ j + "\n";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Precedence) {
            Precedence p = (Precedence) obj;
            return p.i == i && p.j == j;
        }
        return false;
    }
}
