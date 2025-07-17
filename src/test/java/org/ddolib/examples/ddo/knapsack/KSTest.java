package org.ddolib.examples.ddo.knapsack;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.HashSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.ddolib.examples.ddo.knapsack.KSMain.readInstance;
import static org.ddolib.factory.Solvers.astarSolver;
import static org.ddolib.factory.Solvers.sequentialSolver;
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
        final KSRelax relax = new KSRelax();
        final KSRanking ranking = new KSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(250);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<>();
        final KSFastUpperBound fub = new KSFastUpperBound(problem);

        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                fub);

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
        final KSRelax relax = new KSRelax();
        final KSFastUpperBound fub = new KSFastUpperBound(problem);
        final KSRanking ranking = new KSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(250);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final SimpleDominanceChecker<Integer, Integer> dominance = new SimpleDominanceChecker<>(new KSDominance(),
                problem.nbVars());

        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                fub,
                dominance
        );

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimal);
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAstar(KSProblem problem) {
        final KSRelax relax = new KSRelax();
        final KSFastUpperBound fub = new KSFastUpperBound(problem);
        final KSRanking ranking = new KSRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(250);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();
        final SimpleDominanceChecker<Integer, Integer> dominance = new SimpleDominanceChecker<>(new KSDominance(),
                problem.nbVars());


        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = astarSolver(
                problem,
                varh,
                fub,
                dominance
        );

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimal);
    }
}
