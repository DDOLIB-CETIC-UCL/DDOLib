package org.ddolib.examples.layered.pdptw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.ddolib.common.util.debug.DebugLevel;
import org.ddolib.layered.modeling.ExactModel;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Solvers;
import org.junit.jupiter.api.Test;

/** Tests of the fast lower bound of the PDPTW ({@link PDPTWFastLowerBound}). */
public class PDPTWFastLowerBoundTest {

    /**
     * Builds an instance with the depot and 5 nodes on a line: 3 nodes close to the depot that open
     * late (time 100) and 2 far nodes that open at time 0. The optimal route visits the far nodes
     * first, while waiting for the close ones, and costs 103.
     */
    private static PDPTWProblem lineInstance(double[] deadlines) {
        double[] x = {0, 1, 2, 3, 10, -10};
        double[] early = {0, 100, 100, 100, 0, 0};
        double[][] distances = new double[x.length][x.length];
        TimeWindow[] timeWindows = new TimeWindow[x.length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x.length; j++) {
                distances[i][j] = Math.abs(x[i] - x[j]);
            }
            timeWindows[i] = new TimeWindow(early[i], deadlines[i]);
        }
        return new PDPTWProblem(
                distances, new HashMap<>(), 10, timeWindows, Optional.empty(), false);
    }

    private static double rootBound(PDPTWProblem problem) {
        Set<Integer> vars =
                IntStream.range(0, problem.nbVars()).boxed().collect(Collectors.toSet());
        return new PDPTWFastLowerBound(problem).fastLowerBound(problem.initialState(), vars);
    }

    private static ExactModel<PDPTWState> exactModel(PDPTWProblem problem) {
        return new ExactModel<>() {
            @Override
            public Problem<PDPTWState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<PDPTWState> lowerBound() {
                return new PDPTWFastLowerBound(problem);
            }

            @Override
            public DebugLevel debugMode() {
                // checks that the bound is admissible on every state
                return DebugLevel.ON;
            }
        };
    }

    @Test
    void testBoundIsAdmissibleWhenTravelAndWaitingOverlap() {
        PDPTWProblem problem = lineInstance(new double[] {1e4, 1e4, 1e4, 1e4, 1e4, 1e4});
        double optimum = Solvers.minimizeExact(exactModel(problem)).value();

        assertEquals(103.0, optimum, 1e-9);
        assertTrue(rootBound(problem) <= optimum);
    }

    @Test
    void testFeasibleInstanceIsNotDeclaredInfeasible() {
        // the optimal route of cost 103 meets these deadlines exactly
        PDPTWProblem problem = lineInstance(new double[] {103, 102, 101, 100, 10, 30});

        assertFalse(Double.isInfinite(rootBound(problem)));
        assertEquals(103.0, Solvers.minimizeExact(exactModel(problem)).value(), 1e-9);
    }
}
