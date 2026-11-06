package org.ddolib.examples.layered.msct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Tests of the key of {@link MSCTDominance}. */
public class MSCTDominanceTest {

    private final MSCTDominance dominance = new MSCTDominance();

    @Test
    void testStatesWithTheSameRemainingJobsShareTheirKey() {
        MSCTState s1 = new MSCTState(new HashSet<>(Set.of(1, 2, 3)), 10);
        MSCTState s2 = new MSCTState(new HashSet<>(Set.of(3, 2, 1)), 8);

        assertEquals(dominance.getKey(s1), dominance.getKey(s2));
        assertEquals(dominance.getKey(s1).hashCode(), dominance.getKey(s2).hashCode());
        assertTrue(dominance.isDominatedOrEqual(s1, s2));
    }

    @Test
    void testStatesWithDifferentRemainingJobsHaveDifferentKeys() {
        MSCTState s1 = new MSCTState(new HashSet<>(Set.of(1, 2, 3)), 10);
        MSCTState s2 = new MSCTState(new HashSet<>(Set.of(1, 2)), 8);

        // they cannot dominate each other, so they need not be compared
        assertNotEquals(dominance.getKey(s1), dominance.getKey(s2));
        assertFalse(dominance.isDominatedOrEqual(s1, s2));
        assertFalse(dominance.isDominatedOrEqual(s2, s1));
    }
}
