package org.ddolib.ddo.examples.alp;

import java.util.Objects;

/**
 * Decision of affecting an aircraft of a defined class to a defined runway.
 * <p>
 * The specific chosen aircraft is defined by :
 * - The state ==> remaining aircraft of class.
 * - The problem ==> ordered list (by target arrival time) of aircraft per class.
 * - Using the remaining aircraft nb as the index of the ordered list.
 */
public class ALPDecision {
    public int aircraftClass;
    public int runway;

    public ALPDecision(int aircraftClass, int runway) {
        this.aircraftClass = aircraftClass;
        this.runway = runway;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ALPDecision that = (ALPDecision) o;
        return aircraftClass == that.aircraftClass && runway == that.runway;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aircraftClass, runway);
    }
}
