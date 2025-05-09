package org.ddolib.ddo.examples.gruler;

import org.ddolib.ddo.examples.Golomb;

import java.util.BitSet;
import java.util.Objects;

public class GRState {
    private BitSet marks;         // Set of marks already placed
    private BitSet distances;     // Set of pairwise distances already present
    private int lastMark;         // Location of last mark

    public GRState() {
        this.marks = new BitSet();
        marks.set(0);
        this.distances = new BitSet();
        this.lastMark = 0;
    }

    public GRState(BitSet marks, BitSet distances, int lastMark) {
        this.marks = (BitSet) marks.clone();
        this.distances = (BitSet) distances.clone();
        this.lastMark = lastMark;
    }

    public BitSet getMarks() {
        return marks;
    }

    public BitSet getDistances() {
        return distances;
    }

    public int getNumberOfMarks() {
        return marks.size();
    }

    public int getLastMark() {
        return lastMark;
    }

    public void addMark(int mark) {
        assert(mark >= lastMark);
        lastMark = mark;
        marks.set(mark);
    }

    public void addDistance(int distance) {
        assert !distances.get(distance);
        distances.set(distance);
    }

    public void setLastMark(int mark) {
        lastMark = mark;
    }

    public GRState copy() {
        return new GRState(marks, distances, lastMark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(marks, distances, lastMark);
    }
}
