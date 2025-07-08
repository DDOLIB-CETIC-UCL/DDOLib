package org.ddolib.ddo.examples.knapsack;

import org.ddolib.ddo.core.CutSetType;
import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.RelaxationStrat;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.heuristics.StateCoordinates;
import org.ddolib.ddo.heuristics.StateDistance;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.DefaultDominanceChecker;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.RelaxationSolver;
import org.ddolib.ddo.implem.solver.SequentialSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.HashSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.ddolib.ddo.examples.knapsack.KSMain.readInstance;
import static org.ddolib.ddo.implem.solver.Solvers.relaxationSolver;
import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;
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
        final KSRelax relax = new KSRelax(problem);
        final KSRanking ranking = new KSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(250);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<>();

        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimal);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void fastUpperBoundTest(KSProblem problem) throws IOException {
        final KSRelax relax = new KSRelax(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        double rub = relax.fastUpperBound(problem.capa, vars);
        // Checks if the upper bound at the root is bigger than the optimal solution
        assertTrue(rub >= problem.optimal,
                String.format("Upper bound %.1f is not bigger than the expected optimal solution %.1f",
                        rub,
                        problem.optimal));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testKnapsackWithDominance(KSProblem problem) {
        final KSRelax relax = new KSRelax(problem);
        final KSRanking ranking = new KSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(250);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final SimpleDominanceChecker<Integer, Integer> dominance = new SimpleDominanceChecker<>(new KSDominance(),
                problem.nbVars());
        final StateCoordinates<Integer> coordinates = new KSCoordinates();
        final StateDistance<Integer> distance = new KSDistance();
        final RelaxationStrat relaxationStrat = RelaxationStrat.Cost;
        final int seed = 56646464;

        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                dominance,
                relaxationStrat,
                distance,
                coordinates,
                seed
        );

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimal);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRelaxationsCompleteSearch(KSProblem problem) {
        final KSRelax relax = new KSRelax(problem);
        final KSRanking ranking = new KSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(25);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final StateDistance<Integer> distance = new KSDistance();
        final StateCoordinates<Integer> coord = new KSCoordinates();
        final DefaultDominanceChecker<Integer> dominance = new DefaultDominanceChecker<>();

        for (RelaxationStrat relaxStrat : RelaxationStrat.values()) {
            final Solver solver = sequentialSolver(
                    problem,
                    relax,
                    varh,
                    ranking,
                    width,
                    frontier,
                    dominance,
                    relaxStrat,
                    distance,
                    coord,
                    6546488);


            solver.maximize();
            assertEquals(solver.bestValue().get(), problem.optimal);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRelaxation(KSProblem problem) {
        final KSRelax relax = new KSRelax(problem);
        final KSRanking ranking = new KSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(25);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final StateDistance<Integer> distance = new KSDistance();
        final StateCoordinates<Integer> coord = new KSCoordinates();
        final DefaultDominanceChecker<Integer> dominance = new DefaultDominanceChecker<>();

        for (RelaxationStrat relaxStrat : RelaxationStrat.values()) {
            if (relaxStrat != RelaxationStrat.MinDist) {
                final Solver solver = relaxationSolver(
                        problem,
                        relax,
                        varh,
                        ranking,
                        width,
                        frontier,
                        dominance,
                        relaxStrat,
                        distance,
                        coord,
                        6546488);


                solver.maximize();
                assertTrue(solver.bestValue().get() >= problem.optimal);
            }
        }
    }
}
