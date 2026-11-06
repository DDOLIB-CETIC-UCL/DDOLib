package org.ddolib.examples.layered.misp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.BitSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests of {@link MispCliqueCoverLowerBound}. */
public class MispCliqueCoverLowerBoundTest {

    private static MispProblem randomGraph(int n, double density, Random random) {
        BitSet[] neighbors = new BitSet[n];
        for (int i = 0; i < n; i++) {
            neighbors[i] = new BitSet(n);
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (random.nextDouble() < density) {
                    neighbors[i].set(j);
                    neighbors[j].set(i);
                }
            }
        }
        int[] weight = new int[n];
        for (int i = 0; i < n; i++) {
            weight[i] = 1 + random.nextInt(10);
        }
        BitSet all = new BitSet(n);
        all.set(0, n);
        return new MispProblem(all, neighbors, weight, Optional.empty());
    }

    /** The weight of a maximum weight independent set of the vertices of {@code state}. */
    private static int bruteForce(MispProblem problem, BitSet state) {
        int first = state.nextSetBit(0);
        if (first < 0) {
            return 0;
        }
        BitSet without = (BitSet) state.clone();
        without.clear(first);
        BitSet with = (BitSet) without.clone();
        with.andNot(problem.neighbors[first]);
        return Math.max(
                bruteForce(problem, without), problem.weight[first] + bruteForce(problem, with));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.1, 0.3, 0.5, 0.8})
    void testBoundIsAdmissibleAndTighterThanTheSimpleBound(double density) {
        Random random = new Random(42);
        for (int g = 0; g < 20; g++) {
            MispProblem problem = randomGraph(12, density, random);
            MispCliqueCoverLowerBound clique = new MispCliqueCoverLowerBound(problem);
            MispFastLowerBound simple = new MispFastLowerBound(problem);
            for (int s = 0; s < 50; s++) {
                BitSet state = new BitSet(12);
                for (int v = 0; v < 12; v++) {
                    if (random.nextBoolean()) {
                        state.set(v);
                    }
                }
                double lb = clique.fastLowerBound(state, Set.of());
                // the costs are the negated weights
                assertTrue(lb <= -bruteForce(problem, state), "not admissible");
                assertTrue(lb >= simple.fastLowerBound(state, Set.of()), "weaker");
            }
        }
    }

    @Test
    void testCompleteGraphIsOneClique() {
        int n = 6;
        MispProblem problem = randomGraph(n, 1.0, new Random(1));
        MispCliqueCoverLowerBound clique = new MispCliqueCoverLowerBound(problem);
        BitSet all = new BitSet(n);
        all.set(0, n);
        int max = 0;
        for (int w : problem.weight) {
            max = Math.max(max, w);
        }

        assertEquals(1, clique.nbCliques());
        assertEquals(-max, clique.fastLowerBound(all, Set.of()));
    }

    @Test
    void testGraphWithoutEdgeGivesTheSimpleBound() {
        MispProblem problem = randomGraph(8, 0.0, new Random(2));
        MispCliqueCoverLowerBound clique = new MispCliqueCoverLowerBound(problem);
        MispFastLowerBound simple = new MispFastLowerBound(problem);
        BitSet all = new BitSet(8);
        all.set(0, 8);

        assertEquals(8, clique.nbCliques());
        assertEquals(simple.fastLowerBound(all, Set.of()), clique.fastLowerBound(all, Set.of()));
        assertEquals(0.0, clique.fastLowerBound(new BitSet(8), Set.of()));
    }
}
