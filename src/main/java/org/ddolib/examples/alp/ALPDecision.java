package org.ddolib.examples.alp;

import java.util.Objects;
/**
 * Represents a decision in the <b>Aircraft Landing Problem (ALP)</b>.
 * <p>
 * This decision consists of assigning an aircraft of a given class to a specific runway.
 * The actual aircraft chosen is determined by:
 * </p>
 * <ul>
 *     <li>The {@code state}: the number of remaining aircraft of that class.</li>
 *     <li>The {@code problem}: an ordered list (by target arrival time) of aircraft per class.</li>
 *     <li>The remaining aircraft count acts as the index into the ordered list to select the specific aircraft.</li>
 * </ul>
 *
 * <p>
 * Instances of this class are immutable with respect to their {@code aircraftClass} and {@code runway} values.
 * Equality and hash code are defined based on these two fields.
 * </p>
 *
 * @see ALPProblem
 * @see ALPState
 */
public class ALPDecision {
    /** The class of the aircraft being assigned. */
    public int aircraftClass;
    /** The runway to which the aircraft is assigned. */
    public int runway;
    /**
     * Constructs a new decision assigning an aircraft of the given class to the given runway.
     *
     * @param aircraftClass the class of the aircraft
     * @param runway        the runway number
     */
    public ALPDecision(int aircraftClass, int runway) {
        this.aircraftClass = aircraftClass;
        this.runway = runway;
    }

    /**
     * Checks whether this decision is equal to another object.
     * Two decisions are equal if they have the same aircraft class and runway.
     *
     * @param o the object to compare with
     * @return {@code true} if the objects represent the same decision, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ALPDecision that = (ALPDecision) o;
        return aircraftClass == that.aircraftClass && runway == that.runway;
    }
    /**
     * Computes the hash code of this decision based on the aircraft class and runway.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(aircraftClass, runway);
    }
}
