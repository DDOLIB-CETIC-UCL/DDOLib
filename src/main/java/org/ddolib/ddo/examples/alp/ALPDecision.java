package org.ddolib.ddo.examples.alp;

import java.util.Objects;

public class ALPDecision{
    public int aircraftClass;
    public int runway;

    public ALPDecision(int aircraftClass, int runway){
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
