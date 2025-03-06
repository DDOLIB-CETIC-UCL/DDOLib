package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.examples.Knapsack.KnapsackProblem;
import org.ddolib.ddo.examples.Knapsack.KnapsackRanking;
import org.ddolib.ddo.examples.Knapsack.KnapsackRelax;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;
import org.ddolib.ddo.implem.solver.SequentialSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PigmentSchedulingTest {

    // quite easy instances
    static Stream<PigmentScheduling.PSPInstance> instance2items() {
        Stream<Integer> testStream = IntStream.rangeClosed(1, 10).boxed();
        return testStream.flatMap(i -> {
            return Stream.of(new PigmentScheduling.PSPInstance("src/test/resources/PSP/2items/" + i));
        });
    }

    // harder instances
    static Stream<PigmentScheduling.PSPInstance> instance5items() {
        Stream<Integer> testStream = IntStream.rangeClosed(1, 10).boxed();
        return testStream.flatMap(i -> {
            return Stream.of(new PigmentScheduling.PSPInstance("src/test/resources/PSP/5items/" + i));
        });
    }

    @ParameterizedTest
    @MethodSource({"instance2items"/*,"instance5items"*/})
    public void testParameterized(PigmentScheduling.PSPInstance instance) {
        PigmentScheduling.PSP problem = new PigmentScheduling.PSP(instance);
        final PigmentScheduling.PSPRelax relax = new PigmentScheduling.PSPRelax(instance);
        final PigmentScheduling.PSPRanking ranking = new PigmentScheduling.PSPRanking();
        final FixedWidth<PigmentScheduling.PSPState> width = new FixedWidth<>(10);
        final VariableHeuristic<PigmentScheduling.PSPState> varh = new DefaultVariableHeuristic();
        final Frontier<PigmentScheduling.PSPState> frontier = new SimpleFrontier<>(ranking);
        final Solver solver = new SequentialSolver<>(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;
        assertEquals(solver.bestValue().get(), -instance.optimal); // put a minus since it is a minimization problem
    }
}