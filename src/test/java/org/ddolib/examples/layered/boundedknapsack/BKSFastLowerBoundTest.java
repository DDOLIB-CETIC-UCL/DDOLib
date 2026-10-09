package org.ddolib.examples.layered.boundedknapsack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.modeling.StateRanking;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Tests of the fast lower bound of the bounded knapsack problem ({@link BKSFastLowerBound}). */
public class BKSFastLowerBoundTest {

    /**
     * Computes with a dynamic program the best profit reachable with the given items and capacity.
     *
     * @param problem the instance
     * @param items the items that can still be selected
     * @param capacity the remaining capacity
     * @return the best reachable profit
     */
    private static int bestProfit(BKSProblem problem, Set<Integer> items, int capacity) {
        int[] best = new int[capacity + 1];
        for (int item : items) {
            for (int c = capacity; c >= 0; c--) {
                for (int q = 1; q <= problem.quantities[item]; q++) {
                    int w = q * problem.weights[item];
                    if (w > c) {
                        break;
                    }
                    best[c] = Math.max(best[c], best[c - w] + q * problem.values[item]);
                }
            }
        }
        return best[capacity];
    }

    @Test
    void testBoundIsTheLinearRelaxation() {
        // ratios: item 0 = 2, item 1 = 1.5, item 2 = 1
        BKSProblem problem =
                new BKSProblem(10, new int[] {8, 6, 5}, new int[] {4, 4, 5}, new int[] {1, 2, 3});
        double bound = new BKSFastLowerBound(problem).fastLowerBound(10, Set.of(0, 1, 2));
        // 1 unit of item 0 (weight 4) then 1.5 units of item 1 (weight 6): 8 + 1.5 * 6 = 17
        assertEquals(-17.0, bound, 1e-9);
    }

    @ParameterizedTest
    @EnumSource(BKSProblem.InstanceType.class)
    void testBoundIsValid(BKSProblem.InstanceType type) {
        Random random = new Random(42);
        for (int seed = 0; seed < 5; seed++) {
            BKSProblem problem = new BKSProblem(12, 50, type, seed);
            BKSFastLowerBound flb = new BKSFastLowerBound(problem);
            for (int k = 0; k < 50; k++) {
                // random remaining items and remaining capacity
                Set<Integer> items = new HashSet<>();
                for (int i = 0; i < problem.nbVars(); i++) {
                    if (random.nextBoolean()) {
                        items.add(i);
                    }
                }
                int capacity = random.nextInt(problem.capacity + 1);
                double bound = flb.fastLowerBound(capacity, items);
                int optimum = bestProfit(problem, items, capacity);
                assertTrue(
                        bound <= -optimum + 1e-9,
                        "Bound " + bound + " is greater than the optimum " + -optimum);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(BKSProblem.InstanceType.class)
    void testDdoFindsTheOptimum(BKSProblem.InstanceType type) {
        for (int seed = 0; seed < 3; seed++) {
            BKSProblem problem = new BKSProblem(15, 50, type, seed);
            DdoModel<Integer> model =
                    new DdoModel<>() {
                        @Override
                        public Problem<Integer> problem() {
                            return problem;
                        }

                        @Override
                        public FastLowerBound<Integer> lowerBound() {
                            return new BKSFastLowerBound(problem);
                        }

                        @Override
                        public DominanceChecker<Integer> dominance() {
                            return new SimpleDominanceChecker<>(
                                    new BKSDominance(), problem.nbVars());
                        }

                        @Override
                        public Relaxation<Integer> relaxation() {
                            return new BKSRelax();
                        }

                        @Override
                        public StateRanking<Integer> ranking() {
                            return new BKSRanking();
                        }

                        @Override
                        public WidthHeuristic<Integer> widthHeuristic() {
                            return new FixedWidth<>(5);
                        }
                    };
            Set<Integer> all =
                    IntStream.range(0, problem.nbVars()).boxed().collect(Collectors.toSet());
            double optimum = -bestProfit(problem, all, problem.capacity);
            assertEquals(optimum, Solvers.minimizeDdo(model).value(), 1e-9);
        }
    }
}
