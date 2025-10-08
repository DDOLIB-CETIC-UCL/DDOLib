package org.ddolib.examples.gruler;


import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;


public class GRState {
    private BitSet marks;         // Set of marks already placed
    private BitSet distances;     // Set of pairwise distances already present
    private int lastMark;

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

    public GRState copy() {
        return new GRState(marks, distances, lastMark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(marks, distances, lastMark);
    }

    @Override
    public String toString() {
        return "(" + Arrays.toString(marks.stream().toArray()) + " , " + Arrays.toString(distances.stream().toArray()) + " , " + lastMark + ")";
    }
    @Override
    public boolean equals(Object obj) {
        GRState other = (GRState) obj;
        if (this.marks.equals(other.marks) && this.distances.equals(other.distances) && this.lastMark == other.lastMark) {
            return true;
        }
        return false;
    }
}
