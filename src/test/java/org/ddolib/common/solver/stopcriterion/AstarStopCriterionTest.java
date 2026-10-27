package org.ddolib.common.solver.stopcriterion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Predicate;
import org.ddolib.common.solver.stat.AstarStats;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.examples.nolayer.knapsack.KSModel;
import org.ddolib.examples.nolayer.knapsack.KSState;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.junit.jupiter.api.Test;

class AstarStopCriterionTest {

    // On smaller instances (e.g. instance_test_0), A* can reach the optimum without ever pruning
    // or discarding a freshly generated child, so the indicator legitimately stays at 100 %.
    private static final String KS_INSTANCE =
            Path.of("src", "test", "resources", "Knapsack", "instance_test_1").toString();

    @Test
    void testMinValidChildrenPercent() {
        AstarStopCriterion criterion = AstarStopCriterion.minValidChildrenPercent(20.0);
        AstarStats stats = new AstarStats(System.currentTimeMillis(), 1000.0);

        // Initial ratio is 1.0 (100%), so it should not stop
        assertFalse(criterion.test(stats));

        // Update with 1 selected child out of 10 generated (10%)
        // With alpha=0.05, new ratio = 0.05*0.1 + 0.95*1.0 = 0.005 + 0.95 = 0.955
        stats = stats.updateValidChildrenPercent(10, 1);
        assertFalse(criterion.test(stats));

        // Force a low ratio for testing by repeating the update
        for (int i = 0; i < 100; i++) {
            stats = stats.updateValidChildrenPercent(100, 1); // 1%
        }

        assertTrue(stats.validChildrenPercent() < 20.0);
        assertTrue(criterion.test(stats));
    }

    @Test
    void testWrongStatsThrowsException() {
        AstarStopCriterion criterion = AstarStopCriterion.minValidChildrenPercent(20.0);
        assertThrows(
                IllegalArgumentException.class,
                () -> criterion.test(new org.ddolib.common.solver.stat.DdoStats(0, 0)));
    }

    @Test
    void testNodeWithoutSelectedChildrenLowersRatio() {
        AstarStats stats = new AstarStats(System.currentTimeMillis(), 1000.0);
        // A node whose children are all pruned is the stagnation signal: it must count as r = 0
        stats = stats.updateValidChildrenPercent(5, 0);
        assertEquals(95.0, stats.validChildrenPercent(), 1e-9);
    }

    @Test
    void testNodeWithoutGeneratedChildrenKeepsRatio() {
        AstarStats stats = new AstarStats(System.currentTimeMillis(), 1000.0);
        stats = stats.updateValidChildrenPercent(0, 0);
        assertEquals(100.0, stats.validChildrenPercent(), 1e-9);
    }

    @Test
    void testLayeredAstarUpdatesValidChildrenPercent() throws IOException {
        var problem = new org.ddolib.examples.layered.knapsack.KSProblem(KS_INSTANCE);
        var model =
                new org.ddolib.layered.modeling.Model<Integer>() {
                    @Override
                    public Problem<Integer> problem() {
                        return problem;
                    }

                    @Override
                    public FastLowerBound<Integer> lowerBound() {
                        return new org.ddolib.examples.layered.knapsack.KSFastLowerBound(problem);
                    }
                };
        assertValidChildrenPercentDecreases(
                limit -> org.ddolib.layered.modeling.Solvers.minimizeAstar(model, limit));
    }

    @Test
    void testLayeredAcsUpdatesValidChildrenPercent() throws IOException {
        var problem = new org.ddolib.examples.layered.knapsack.KSProblem(KS_INSTANCE);
        var model =
                new org.ddolib.layered.modeling.AcsModel<Integer>() {
                    @Override
                    public Problem<Integer> problem() {
                        return problem;
                    }

                    @Override
                    public FastLowerBound<Integer> lowerBound() {
                        return new org.ddolib.examples.layered.knapsack.KSFastLowerBound(problem);
                    }
                };
        assertValidChildrenPercentDecreases(
                limit -> org.ddolib.layered.modeling.Solvers.minimizeAcs(model, limit));
    }

    @Test
    void testLayeredAwAstarUpdatesValidChildrenPercent() throws IOException {
        var problem = new org.ddolib.examples.layered.knapsack.KSProblem(KS_INSTANCE);
        var model =
                new org.ddolib.layered.modeling.AwAstarModel<Integer>() {
                    @Override
                    public Problem<Integer> problem() {
                        return problem;
                    }

                    @Override
                    public FastLowerBound<Integer> lowerBound() {
                        return new org.ddolib.examples.layered.knapsack.KSFastLowerBound(problem);
                    }
                };
        assertValidChildrenPercentDecreases(
                limit -> org.ddolib.layered.modeling.Solvers.minimizeAwAStar(model, limit));
    }

    @Test
    void testNoLayerAstarUpdatesValidChildrenPercent() throws IOException {
        var model = noLayerKsModel();
        assertValidChildrenPercentDecreases(
                limit -> org.ddolib.nolayer.modeling.Solvers.minimizeAstar(model, limit));
    }

    @Test
    void testNoLayerAcsUpdatesValidChildrenPercent() throws IOException {
        var base = noLayerKsModel();
        var model =
                new org.ddolib.nolayer.modeling.AcsModel<KSState>() {
                    @Override
                    public org.ddolib.nolayer.modeling.Problem<KSState> problem() {
                        return base.problem();
                    }

                    @Override
                    public org.ddolib.nolayer.modeling.FastLowerBound<KSState> lowerBound() {
                        return base.lowerBound();
                    }
                };
        assertValidChildrenPercentDecreases(
                limit -> org.ddolib.nolayer.modeling.Solvers.minimizeAcs(model, limit));
    }

    @Test
    void testNoLayerAwAstarUpdatesValidChildrenPercent() throws IOException {
        var base = noLayerKsModel();
        var model =
                new org.ddolib.nolayer.modeling.AwAstarModel<KSState>() {
                    @Override
                    public org.ddolib.nolayer.modeling.Problem<KSState> problem() {
                        return base.problem();
                    }

                    @Override
                    public org.ddolib.nolayer.modeling.FastLowerBound<KSState> lowerBound() {
                        return base.lowerBound();
                    }
                };
        assertValidChildrenPercentDecreases(
                limit -> org.ddolib.nolayer.modeling.Solvers.minimizeAwAstar(model, limit));
    }

    private static KSModel noLayerKsModel() throws IOException {
        return new KSModel(org.ddolib.examples.nolayer.knapsack.KSProblem.fromFile(KS_INSTANCE));
    }

    /**
     * Runs the search to completion while recording the lowest value reached by the indicator.
     * Asserting on that minimum (rather than on an early stop of the search) keeps the test
     * independent of when the solver happens to evaluate its stop criterion.
     */
    private static void assertValidChildrenPercentDecreases(
            Function<Predicate<SearchStatistics>, ?> solve) {
        double[] minPercent = {Double.POSITIVE_INFINITY};
        solve.apply(
                stats -> {
                    minPercent[0] =
                            Math.min(minPercent[0], ((AstarStats) stats).validChildrenPercent());
                    return false;
                });
        assertTrue(
                minPercent[0] < 100.0,
                "validChildrenPercent was never updated (min = " + minPercent[0] + ")");
    }
}
