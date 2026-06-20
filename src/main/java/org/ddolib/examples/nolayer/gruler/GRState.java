package org.ddolib.examples.nolayer.gruler;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

/**
 * Represents a state in the Golomb Ruler (GR) problem for nolayer models.
 */
public class GRState {
    /**
     * Set of marks already placed on the ruler.
     */
    private BitSet marks;
    /**
     * Set of pairwise distances already covered by the placed marks.
     */
    private BitSet distances;
    /**
     * The position of the last placed mark on the ruler.
     */
    private int lastMark;
    /**
     * The layer (depth) of the state.
     */
    private int layer;

    /**
     * Constructs a new {@link GRState} from given sets of marks and distances.
     * <p>
     * Defensive copies of the bitsets are made to ensure immutability of the internal state.
     * </p>
     *
     * @param marks     the set of marks already placed.
     * @param distances the set of pairwise distances already covered.
     * @param lastMark  the position of the last placed mark.
     */
    public GRState(BitSet marks, BitSet distances, int lastMark, int layer) {
        this.marks = (BitSet) marks.clone();
        this.distances = (BitSet) distances.clone();
        this.lastMark = lastMark;
        this.layer = layer;
    }

    /**
     * Returns the set of marks already placed.
     *
     * @return a {@link BitSet} representing the placed marks.
     */
    public BitSet getMarks() {
        return marks;
    }

    /**
     * Returns the set of pairwise distances already covered.
     *
     * @return a {@link BitSet} representing existing distances.
     */
    public BitSet getDistances() {
        return distances;
    }

    /**
     * Returns the number of marks currently placed.
     *
     * @return the number of marks in this state.
     */
    public int getNumberOfMarks() {
        return layer; // Return the explicit layer instead of cardinality
    }

    public int getLayer() {
        return layer;
    }

    /**
     * Returns the position of the last placed mark.
     *
     * @return the position (integer value) of the last mark.
     */
    public int getLastMark() {
        return lastMark;
    }

    /**
     * Creates and returns a deep copy of this state.
     *
     * @return a new {@link GRState} identical to the current one.
     */
    public GRState copy() {
        return new GRState(marks, distances, lastMark, layer);
    }

    /**
     * Computes the hash code for this state based on marks, distances, and the last mark.
     *
     * @return the hash code of this state.
     */
    @Override
    public int hashCode() {
        return Objects.hash(marks, distances, lastMark, layer);
    }

    /**
     * Compares this state to another for equality.
     * Two states are equal if they have identical marks, identical distances,
     * and the same last mark position.
     *
     * @param obj the object to compare to.
     * @return {@code true} if the states are identical; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GRState other) {
            return this.marks.equals(other.marks) && this.distances.equals(other.distances) && this.lastMark == other.lastMark && this.layer == other.layer;
        }
        return false;
    }

    /**
     * Returns a human-readable string representation of this state.
     * <p>
     * The output includes the list of marks, distances, and the last mark position.
     * </p>
     *
     * @return a string describing this state.
     */
    @Override
    public String toString() {
        return "(" + Arrays.toString(marks.stream().toArray()) + " , " + Arrays.toString(distances.stream().toArray()) + " , " + lastMark + " , " + layer + ")";
    }
}
