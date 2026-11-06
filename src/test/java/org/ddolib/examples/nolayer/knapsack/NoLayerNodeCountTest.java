package org.ddolib.examples.nolayer.knapsack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Stream;
import org.ddolib.common.solver.stat.SearchStatistics;
import org.ddolib.nolayer.modeling.AcsModel;
import org.ddolib.nolayer.modeling.AwAstarModel;
import org.ddolib.nolayer.modeling.Solvers;
import org.ddolib.nolayer.solving.startheuristic.DFS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests of the expanded and generated node counts of the no-layer solvers: a node is generated
 * each time a transition is applied to compute a successor.
 */
public class NoLayerNodeCountTest {

    /** A knapsack counting the transitions applied by the solver. */
    private static final class CountingProblem extends KSProblem {
        private long nbTransitions = 0;

        CountingProblem(KSProblem problem) {
            super(problem.profit, problem.weight, problem.capa);
        }

        @Override
        public KSState transition(KSState state, int label) {
            nbTransitions++;
            return super.transition(state, label);
        }
    }

    private static CountingProblem problem() throws IOException {
        return new CountingProblem(
                KSProblem.fromFile(
                        Path.of("src", "test", "resources", "Knapsack", "instance_test_0")
                                .toString()));
    }

    private static AcsModel<KSState> acsModel(KSProblem problem) {
        KSModel base = new KSModel(problem);
        return new AcsModel<>() {
            @Override
            public org.ddolib.nolayer.modeling.Problem<KSState> problem() {
                return problem;
            }

            @Override
            public org.ddolib.nolayer.modeling.FastLowerBound<KSState> lowerBound() {
                return base.lowerBound();
            }
        };
    }

    private static AwAstarModel<KSState> awAstarModel(KSProblem problem) {
        KSModel base = new KSModel(problem);
        return new AwAstarModel<>() {
            @Override
            public org.ddolib.nolayer.modeling.Problem<KSState> problem() {
                return problem;
            }

            @Override
            public org.ddolib.nolayer.modeling.FastLowerBound<KSState> lowerBound() {
                return base.lowerBound();
            }
        };
    }

    static Stream<Arguments> solvers() {
        KSTestDataSupplier supplier =
                new KSTestDataSupplier(Path.of("src", "test", "resources", "Knapsack"));
        Function<KSProblem, SearchStatistics> astar =
                p -> Solvers.minimizeAstar(new KSModel(p)).statistics();
        Function<KSProblem, SearchStatistics> awastar =
                p -> Solvers.minimizeAwAstar(awAstarModel(p)).statistics();
        Function<KSProblem, SearchStatistics> acs =
                p -> Solvers.minimizeAcs(acsModel(p)).statistics();
        Function<KSProblem, SearchStatistics> ddo =
                p -> Solvers.minimizeDdo(supplier.model(p)).statistics();
        Function<KSProblem, SearchStatistics> dfs =
                p -> new DFS<>(new KSModel(p)).findSolution().statistics();
        return Stream.of(
                Arguments.of("A*", astar),
                Arguments.of("AWA*", awastar),
                Arguments.of("ACS", acs),
                Arguments.of("DDO", ddo),
                Arguments.of("DFS", dfs));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("solvers")
    void testGeneratedNodesAreTheAppliedTransitions(
            String name, Function<KSProblem, SearchStatistics> solver) throws IOException {
        CountingProblem problem = problem();
        SearchStatistics stats = solver.apply(problem);

        assertTrue(stats.nbExpandedNodes() > 0);
        assertEquals(problem.nbTransitions, stats.nbGeneratedNodes());
    }
}
