package org.ddolib.ddo.examples.TSPSmallestAdjacentHopsIncremental;

public class MList {
    public int head;

    public MList tail;

    public MList(int head, MList tail) {
        this.head = head;
        this.tail = tail;
    }

    public MList(int head) {
        this.head = head;
        this.tail = null;
    }
}
