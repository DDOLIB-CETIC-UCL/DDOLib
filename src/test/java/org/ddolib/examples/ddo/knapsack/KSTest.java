package org.ddolib.examples.ddo.knapsack;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.ddo.core.*;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.astar.core.solver.AStarSolver;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.ddo.core.solver.RelaxationSolver;
import org.ddolib.ddo.core.solver.RestrictionSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.HashSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.ddolib.examples.ddo.knapsack.KSMain.readInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KSTest {
    static Stream<KSProblem> dataProvider() {
        Stream<Integer> testStream = IntStream.rangeClosed(0, 10).boxed();
        return testStream.flatMap(i -> {
            try {
                return Stream.of(readInstance("src/test/resources/Knapsack/instance_test_" + i));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testKnapsack(KSProblem problem) {
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new KSRelax();
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>(10);
        config.varh = new DefaultVariableHeuristic<>();
        config.fub = new KSFastUpperBound(problem);
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

        final Solver solver = new SequentialSolver<>(config);

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimal);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void fastUpperBoundTest(KSProblem problem) throws IOException {
        final KSFastUpperBound fub = new KSFastUpperBound(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        double rub = fub.fastUpperBound(problem.capa, vars);
        // Checks if the upper bound at the root is bigger than the optimal solution
        assertTrue(rub >= problem.optimal,
                String.format("Upper bound %.1f is not bigger than the expected optimal solution %.1f",
                        rub,
                        problem.optimal));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testKnapsackWithDominance(KSProblem problem) {
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new KSRelax();
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>(10);
        config.varh = new DefaultVariableHeuristic<>();
        config.fub = new KSFastUpperBound(problem);
        config.dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        final Solver solver = new SequentialSolver<>(config);

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimal);
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAstar(KSProblem problem) {
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.varh = new DefaultVariableHeuristic<>();
        config.fub = new KSFastUpperBound(problem);
        config.dominance = new SimpleDominanceChecker<>(new KSDominance(), problem.nbVars());

        final Solver solver = new AStarSolver<>(config);

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimal);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRelaxationsCompleteSearch(KSProblem problem) {
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new KSRelax();
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>(25);
        config.distance = new KSDistance();
        config.coordinates = new KSCoordinates();
        config.dominance = new DefaultDominanceChecker<>();
        config.restrictStrat = ClusterStrat.Cost;

        for (ClusterStrat relaxStrat : ClusterStrat.values()) {
            config.varh = new DefaultVariableHeuristic<Integer>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
            config.relaxStrat = relaxStrat;
            final Solver solver = new SequentialSolver(config);


        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimal);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRestrictionsCompleteSearch(KSProblem problem) {
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new KSRelax();
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>(25);
        config.distance = new KSDistance();
        config.coordinates = new KSCoordinates();
        config.dominance = new DefaultDominanceChecker<>();
        config.relaxStrat = ClusterStrat.Cost;

        for (ClusterStrat restrictStrat : ClusterStrat.values()) {
            config.varh = new DefaultVariableHeuristic<Integer>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
            config.restrictStrat = restrictStrat;
            final Solver solver = new SequentialSolver(config);


            solver.maximize();
            assertEquals(solver.bestValue().get(), problem.optimal);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRelaxation(KSProblem problem) {
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new KSRelax();
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>(25);
        config.distance = new KSDistance();
        config.coordinates = new KSCoordinates();
        config.dominance = new DefaultDominanceChecker<>();

        for (ClusterStrat relaxStrat : ClusterStrat.values()) {
            config.varh = new DefaultVariableHeuristic<Integer>();
            config.relaxStrat = relaxStrat;
            final Solver solver = new RelaxationSolver(config);


            solver.maximize();
            assertTrue(solver.bestValue().get() >= problem.optimal);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRestriction(KSProblem problem) {
        SolverConfig<Integer, Integer> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new KSRelax();
        config.ranking = new KSRanking();
        config.width = new FixedWidth<>(25);
        config.distance = new KSDistance();
        config.coordinates = new KSCoordinates();
        config.dominance = new DefaultDominanceChecker<>();

        for (ClusterStrat restrictStrat : ClusterStrat.values()) {
            config.varh = new DefaultVariableHeuristic<Integer>();
            config.restrictStrat = restrictStrat;
            final Solver solver = new RestrictionSolver<>(config);


            solver.maximize();
            assertTrue(solver.bestValue().get() <= problem.optimal);
        }
    }
}
