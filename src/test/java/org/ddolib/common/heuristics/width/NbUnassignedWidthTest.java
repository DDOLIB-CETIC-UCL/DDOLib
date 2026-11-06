package org.ddolib.common.heuristics.width;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.TreeSet;
import org.ddolib.common.solver.stat.SearchStatus;
import org.ddolib.examples.layered.knapsack.KSFastLowerBound;
import org.ddolib.examples.layered.knapsack.KSProblem;
import org.ddolib.examples.layered.knapsack.KSRanking;
import org.ddolib.examples.layered.knapsack.KSRelax;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.layered.solver.Solution;
import org.junit.jupiter.api.Test;

/** Tests of {@link NbUnassignedWidth} and of the depth given to the width heuristics. */
public class NbUnassignedWidthTest {

    @Test
    void testWidthIsTheNumberOfUnassignedVariables() {
        NbUnassignedWidth<Object> width = new NbUnassignedWidth<>(10);

        assertEquals(10, width.maximumWidth(null));
        assertEquals(10, width.maximumWidth(null, 0));
        assertEquals(7, width.maximumWidth(null, 3));
        // at least 1, even when every variable is assigned
        assertEquals(1, width.maximumWidth(null, 10));
    }

    @Test
    void testFactorScalesTheWidth() {
        NbUnassignedWidth<Object> width = new NbUnassignedWidth<>(10, 5);

        assertEquals(50, width.maximumWidth(null, 0));
        assertEquals(35, width.maximumWidth(null, 3));
        assertEquals(5, width.maximumWidth(null, 10));
        assertEquals(Integer.MAX_VALUE, new NbUnassignedWidth<>(10, Integer.MAX_VALUE)
                .maximumWidth(null, 0));
    }

    @Test
    void testInvalidParametersAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new NbUnassignedWidth<>(-1));
        assertThrows(IllegalArgumentException.class, () -> new NbUnassignedWidth<>(10, 0));
    }

    @Test
    void testDefaultMethodIgnoresTheDepth() {
        WidthHeuristic<Object> fixed = new FixedWidth<>(4);

        assertEquals(4, fixed.maximumWidth(null, 0));
        assertEquals(4, fixed.maximumWidth(null, 7));
    }

    @Test
    void testDdoGivesTheDepthOfTheSubproblems() throws IOException {
        KSProblem problem =
                new KSProblem(
                        Path.of("src", "test", "resources", "Knapsack", "instance_test_0")
                                .toString());
        TreeSet<Integer> depths = new TreeSet<>();
        NbUnassignedWidth<Integer> base = new NbUnassignedWidth<>(problem.nbVars());
        WidthHeuristic<Integer> recording =
                new WidthHeuristic<>() {
                    @Override
                    public int maximumWidth(Integer state) {
                        throw new AssertionError("the solver must give the depth");
                    }

                    @Override
                    public int maximumWidth(Integer state, int depth) {
                        depths.add(depth);
                        // capped so that the root DD is not exact and subproblems are explored
                        return Math.min(2, base.maximumWidth(state, depth));
                    }
                };
        DdoModel<Integer> model =
                new DdoModel<>() {
                    @Override
                    public Problem<Integer> problem() {
                        return problem;
                    }

                    @Override
                    public FastLowerBound<Integer> lowerBound() {
                        return new KSFastLowerBound(problem);
                    }

                    @Override
                    public Relaxation<Integer> relaxation() {
                        return new KSRelax();
                    }

                    @Override
                    public StateRanking<Integer> ranking() {
                        return new KSRanking();
                    }

                    @Override
                    public WidthHeuristic<Integer> widthHeuristic() {
                        return recording;
                    }
                };
        Solution sol = Solvers.minimizeDdo(model);

        assertEquals(SearchStatus.OPTIMAL, sol.statistics().status());
        assertEquals(problem.optimalValue().get(), sol.value(), 1e-9);
        assertEquals(0, depths.first());
        assertTrue(depths.last() > 0, "only the root was compiled: " + depths);
    }
}
