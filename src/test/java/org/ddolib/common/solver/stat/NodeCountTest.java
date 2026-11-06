package org.ddolib.common.solver.stat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.ddolib.common.heuristics.width.FixedWidth;
import org.ddolib.common.heuristics.width.WidthHeuristic;
import org.ddolib.common.util.InvalidSolutionException;
import org.ddolib.examples.layered.knapsack.KSDominance;
import org.ddolib.examples.layered.knapsack.KSFastLowerBound;
import org.ddolib.examples.layered.knapsack.KSProblem;
import org.ddolib.examples.layered.knapsack.KSRanking;
import org.ddolib.examples.layered.knapsack.KSRelax;
import org.ddolib.layered.modeling.AcsModel;
import org.ddolib.layered.modeling.AwAstarModel;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.ExactModel;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.LnsModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.SimpleDominanceChecker;
import org.ddolib.layered.modeling.Solvers;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.layered.solver.Solution;
import org.ddolib.layered.solving.ddo.core.Decision;
import org.ddolib.layered.solving.startheuristic.DFS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests of {@link SearchStatistics#nbExpandedNodes()} and {@link
 * SearchStatistics#nbGeneratedNodes()}: a node is generated each time a transition is applied to
 * compute a successor, and expanded when its successors are computed.
 */
public class NodeCountTest {

    private static final String INSTANCE =
            Path.of("src", "test", "resources", "Knapsack", "instance_test_0").toString();

    /** A problem counting the transitions applied by the solver. */
    private static final class CountingProblem implements Problem<Integer> {
        private final KSProblem problem;
        private long nbTransitions = 0;

        CountingProblem(KSProblem problem) {
            this.problem = problem;
        }

        @Override
        public int nbVars() {
            return problem.nbVars();
        }

        @Override
        public Integer initialState() {
            return problem.initialState();
        }

        @Override
        public double initialValue() {
            return problem.initialValue();
        }

        @Override
        public Iterator<Integer> domain(Integer state, int var) {
            return problem.domain(state, var);
        }

        @Override
        public Integer transition(Integer state, Decision decision) {
            nbTransitions++;
            return problem.transition(state, decision);
        }

        @Override
        public double transitionCost(Integer state, Decision decision) {
            return problem.transitionCost(state, decision);
        }

        @Override
        public Optional<Double> optimalValue() {
            return problem.optimalValue();
        }

        @Override
        public double evaluate(int[] solution) throws InvalidSolutionException {
            return problem.evaluate(solution);
        }
    }

    private static DdoModel<Integer> ddoModel(CountingProblem problem) {
        return new DdoModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem.problem);
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
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
                return new FixedWidth<>(2);
            }
        };
    }

    private static AwAstarModel<Integer> awAstarModel(CountingProblem problem) {
        DdoModel<Integer> base = ddoModel(problem);
        return new AwAstarModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return base.lowerBound();
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return base.dominance();
            }
        };
    }

    private static AcsModel<Integer> acsModel(CountingProblem problem) {
        DdoModel<Integer> base = ddoModel(problem);
        return new AcsModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return base.lowerBound();
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return base.dominance();
            }
        };
    }

    private static ExactModel<Integer> exactModel(CountingProblem problem) {
        return new ExactModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }
        };
    }

    private static LnsModel<Integer> lnsModel(CountingProblem problem) {
        DdoModel<Integer> base = ddoModel(problem);
        return new LnsModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return base.lowerBound();
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return base.dominance();
            }

            @Override
            public StateRanking<Integer> ranking() {
                return base.ranking();
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return base.widthHeuristic();
            }
        };
    }

    static Stream<Arguments> solvers() {
        Function<CountingProblem, SearchStatistics> astar =
                p -> Solvers.minimizeAstar(awAstarModel(p)).statistics();
        Function<CountingProblem, SearchStatistics> awastar =
                p -> Solvers.minimizeAwAStar(awAstarModel(p)).statistics();
        Function<CountingProblem, SearchStatistics> acs =
                p -> Solvers.minimizeAcs(acsModel(p)).statistics();
        Function<CountingProblem, SearchStatistics> ddo =
                p -> Solvers.minimizeDdo(ddoModel(p)).statistics();
        Function<CountingProblem, SearchStatistics> exact =
                p -> Solvers.minimizeExact(exactModel(p)).statistics();
        Function<CountingProblem, SearchStatistics> dfs =
                p -> new DFS<>(awAstarModel(p)).findSolution().statistics();
        return Stream.of(
                Arguments.of("A*", astar),
                Arguments.of("AWA*", awastar),
                Arguments.of("ACS", acs),
                Arguments.of("DDO", ddo),
                Arguments.of("Exact", exact),
                Arguments.of("DFS", dfs));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("solvers")
    void testGeneratedNodesAreTheAppliedTransitions(
            String name, Function<CountingProblem, SearchStatistics> solver) throws IOException {
        CountingProblem problem = new CountingProblem(new KSProblem(INSTANCE));
        SearchStatistics stats = solver.apply(problem);

        assertTrue(stats.nbExpandedNodes() > 0);
        assertEquals(problem.nbTransitions, stats.nbGeneratedNodes());
        // every expanded node of the knapsack has at least one successor (do not take the item)
        assertTrue(stats.nbGeneratedNodes() >= stats.nbExpandedNodes());
    }

    @Test
    void testLnsCountsTheNodesOfItsDecisionDiagrams() throws IOException {
        CountingProblem problem = new CountingProblem(new KSProblem(INSTANCE));
        SearchStatistics stats =
                Solvers.minimizeLns(lnsModel(problem), s -> s.nbIterations() > 20).statistics();

        assertTrue(stats.nbExpandedNodes() > 0);
        assertTrue(stats.nbGeneratedNodes() >= stats.nbExpandedNodes());
        // the LNS also applies transitions to evaluate the incumbent, they generate no node
        assertTrue(stats.nbGeneratedNodes() <= problem.nbTransitions);
    }

    @Test
    void testCountsNeverDecreaseAndArePrinted() throws IOException {
        CountingProblem problem = new CountingProblem(new KSProblem(INSTANCE));
        List<SearchStatistics> statsList = new ArrayList<>();
        Solution sol =
                Solvers.minimizeAstar(
                        awAstarModel(problem), s -> false, (x, s) -> statsList.add(s));
        statsList.add(sol.statistics());

        for (int i = 1; i < statsList.size(); i++) {
            assertTrue(
                    statsList.get(i).nbExpandedNodes() >= statsList.get(i - 1).nbExpandedNodes());
            assertTrue(
                    statsList.get(i).nbGeneratedNodes()
                            >= statsList.get(i - 1).nbGeneratedNodes());
        }
        String text = sol.statistics().toString();
        assertTrue(text.contains("Expanded nodes"));
        assertTrue(text.contains("Generated nodes"));
    }
}
