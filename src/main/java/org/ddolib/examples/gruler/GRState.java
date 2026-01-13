package org.ddolib.examples.gruler;


import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

/**
 * Represents a state in the Golomb Ruler (GR) problem.
 * <p>
 * A state is defined by:
 * </p>
 * <ul>
 *     <li>The set of marks already placed on the ruler ({@code marks}).</li>
 *     <li>The set of pairwise distances between existing marks ({@code distances}).</li>
 *     <li>The position of the last placed mark ({@code lastMark}).</li>
 * </ul>
 *
 * <p>
 * This class is used by search-based algorithms such as DDO (Decision Diagram Optimization),
 * A*, or Anytime Column Search to represent a partial configuration of the Golomb ruler.
 * </p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * BitSet marks = new BitSet();
 * marks.set(0);
 * marks.set(3);
 * BitSet distances = new BitSet();
 * distances.set(3);
 * GRState state = new GRState(marks, distances, 3);
 * System.out.println(state);
 * // Output: ([0, 3] , [3] , 3)
 * }</pre>
 *
 * @see GRProblem
 * @see GRRelax
 * @see GRRanking
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
     * Constructs a new {@link GRState} from given sets of marks and distances.
     * <p>
     * Defensive copies of the bitsets are made to ensure immutability of the internal state.
     * </p>
     *
     * @param marks     the set of marks already placed.
     * @param distances the set of pairwise distances already covered.
     * @param lastMark  the position of the last placed mark.
     */
    public GRState(BitSet marks, BitSet distances, int lastMark) {
        this.marks = (BitSet) marks.clone();
        this.distances = (BitSet) distances.clone();
        this.lastMark = lastMark;
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
        return marks.size();
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
        return new GRState(marks, distances, lastMark);
    }

    /**
     * Computes the hash code for this state based on marks, distances, and the last mark.
     *
     * @return the hash code of this state.
     */
    @Override
    public int hashCode() {
        return Objects.hash(marks, distances, lastMark);
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
        return "(" + Arrays.toString(marks.stream().toArray()) + " , " + Arrays.toString(distances.stream().toArray()) + " , " + lastMark + ")";
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
            return this.marks.equals(other.marks) && this.distances.equals(other.distances) && this.lastMark == other.lastMark;
        }
        return false;
    }
}
