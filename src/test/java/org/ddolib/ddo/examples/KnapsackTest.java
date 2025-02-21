package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.examples.Knapsack.*;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.HashSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class KnapsackTest {
    static Stream<KnapsackProblem> dataProvider() {
        Stream<Integer> testStream = IntStream.rangeClosed(0, 10).boxed();
        return testStream.flatMap(i -> {
            try {
                return Stream.of(Knapsack.readInstance("src/test/resources/Knapsack/instance_test_" + i));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void fastUpperBoundTest(KnapsackProblem problem) throws IOException {
        final KnapsackRelax relax = new KnapsackRelax(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        int rub = relax.fastUpperBound(problem.capa, vars);
        // Checks if the upper bound at the root is bigger than the optimal solution
        assertTrue(rub >= problem.optimal,
                String.format("Upper bound %d is not bigger than the expected optimal solution %d",
                        rub,
                        problem.optimal));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testParameterized(KnapsackProblem problem) {
        final KnapsackRelax relax = new KnapsackRelax(problem);
        final KnapsackRanking ranking = new KnapsackRanking();
        final FixedWidth<Integer> width = new FixedWidth<>(250);
        final VariableHeuristic<Integer> varh = new DefaultVariableHeuristic<Integer>();


        final Frontier<Integer> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new ParallelSolver<Integer>(
                Runtime.getRuntime().availableProcessors(),
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimal);
    }

}