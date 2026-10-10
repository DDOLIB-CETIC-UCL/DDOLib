package org.ddolib.examples.layered.msct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.ExactModel;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.modeling.StateRanking;
import org.junit.jupiter.api.Test;

/** Tests of the fast lower bound of the MSCT problem ({@link MSCTFastLowerBound}). */
public class MSCTFastLowerBoundTest {

    @Test
    void testOnlyTheRemainingNumberOfJobsIsCounted() {
        MSCTProblem problem =
                new MSCTProblem(new int[] {0, 0, 0}, new int[] {1, 1, 1}, Optional.empty());
        // merged state: 3 jobs can still be selected but only 2 decisions remain
        MSCTState merged = new MSCTState(Set.of(0, 1, 2), 1);
        double bound = new MSCTFastLowerBound(problem).fastLowerBound(merged, Set.of(1, 2));
        // the 2 remaining jobs complete at times 2 and 3 at best: the bound must not exceed 5
        assertTrue(bound <= 5.0, "The bound " + bound + " exceeds the remaining cost 5");
        // each of the 2 cheapest jobs cannot complete before time 2
        assertEquals(4.0, bound, 1e-9);
    }

    @Test
    void testDdoWithRelaxationFindsTheOptimum() {
        Random random = new Random(42);
        for (int it = 0; it < 200; it++) {
            int n = 3 + random.nextInt(5);
            int[] release = new int[n];
            int[] processing = new int[n];
            for (int i = 0; i < n; i++) {
                release[i] = random.nextInt(8);
                processing[i] = 1 + random.nextInt(6);
            }
            MSCTProblem problem = new MSCTProblem(release, processing, Optional.empty());
            double optimum = Solvers.minimizeExact(exactModel(problem)).value();
            for (int width : new int[] {1, 2}) {
                assertEquals(optimum, Solvers.minimizeDdo(model(problem, width)).value(), 1e-9);
            }
        }
    }

    private static ExactModel<MSCTState> exactModel(MSCTProblem problem) {
        return new ExactModel<>() {
            @Override
            public Problem<MSCTState> problem() {
                return problem;
            }
        };
    }

    private static DdoModel<MSCTState> model(MSCTProblem problem, int width) {
        return new DdoModel<>() {
            @Override
            public Problem<MSCTState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<MSCTState> lowerBound() {
                return new MSCTFastLowerBound(problem);
            }

            @Override
            public Relaxation<MSCTState> relaxation() {
                return new MSCTRelax(problem);
            }

            @Override
            public StateRanking<MSCTState> ranking() {
                return new MSCTRanking();
            }

            @Override
            public WidthHeuristic<MSCTState> widthHeuristic() {
                return new FixedWidth<>(width);
            }
        };
    }
}
