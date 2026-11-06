package org.ddolib.examples.layered.misp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.BitSet;
import java.util.Random;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests of {@link MispDominanceChecker}. */
public class MispDominanceCheckerTest {

    private static BitSet bits(int... vertices) {
        BitSet set = new BitSet();
        for (int v : vertices) {
            set.set(v);
        }
        return set;
    }

    @Test
    void testStateIncludedInABetterStateIsDominated() {
        MispDominanceChecker checker = new MispDominanceChecker(3);

        assertFalse(checker.updateDominance(bits(0, 1, 2), 1, -5));
        assertTrue(checker.updateDominance(bits(0, 2), 1, -4));
        assertTrue(checker.updateDominance(bits(0, 1, 2), 1, -5));
        // a worse value or a state not included is not dominated
        assertFalse(checker.updateDominance(bits(0, 2), 1, -6));
        assertFalse(checker.updateDominance(bits(0, 3), 1, -1));
        // the fronts of the depths are independent
        assertFalse(checker.updateDominance(bits(0, 2), 2, 0));
    }

    @Test
    void testDominatedStatesAreRemovedAndClearForgetsTheStates() {
        MispDominanceChecker checker = new MispDominanceChecker(3);

        assertFalse(checker.updateDominance(bits(0), 1, -1));
        // dominates and removes {0}
        assertFalse(checker.updateDominance(bits(0, 1), 1, -2));
        assertTrue(checker.updateDominance(bits(0), 1, -1));
        checker.clear();
        assertFalse(checker.updateDominance(bits(0), 1, 0));
    }

    /**
     * On random sequences of states, the checker prunes exactly the same states as a {@link
     * SimpleDominanceChecker} with a {@link MispDominance}.
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    void testPrunesTheSameStatesAsTheSimpleChecker(int seed) {
        int nbVars = 10;
        Random random = new Random(seed);
        DominanceChecker<BitSet> simple =
                new SimpleDominanceChecker<>(new MispDominance(), nbVars);
        DominanceChecker<BitSet> checker = new MispDominanceChecker(nbVars);
        for (int i = 0; i < 5000; i++) {
            if (random.nextInt(1000) == 0) {
                simple.clear();
                checker.clear();
            }
            BitSet state = new BitSet(nbVars);
            for (int v = 0; v < nbVars; v++) {
                if (random.nextInt(3) > 0) {
                    state.set(v);
                }
            }
            int depth = random.nextInt(3);
            double value = -random.nextInt(20);
            assertEquals(
                    simple.updateDominance(state, depth, value),
                    checker.updateDominance(state, depth, value),
                    "state " + i);
        }
    }
}
