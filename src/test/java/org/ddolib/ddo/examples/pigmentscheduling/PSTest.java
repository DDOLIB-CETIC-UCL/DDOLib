package org.ddolib.ddo.examples.pigmentscheduling;

import org.ddolib.ddo.algo.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.algo.heuristics.FixedWidth;
import org.ddolib.ddo.algo.heuristics.VariableHeuristic;
import org.ddolib.ddo.algo.solver.Solver;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.ddolib.ddo.api.Solvers.sequentialSolver;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PSTest {

    // quite easy instances
    static Stream<PSInstance> instance2items() {
        Stream<Integer> testStream = IntStream.rangeClosed(1, 10).boxed();
        return testStream.flatMap(i -> {
            return Stream.of(new PSInstance("src/test/resources/PSP/2items/" + i));
        });
    }

    // harder instances
    static Stream<PSInstance> instance5items() {
        Stream<Integer> testStream = IntStream.rangeClosed(1, 10).boxed();
        return testStream.flatMap(i -> {
            return Stream.of(new PSInstance("src/test/resources/PSP/5items/" + i));
        });
    }

    @ParameterizedTest
    @MethodSource({"instance2items"/*,"instance5items"*/})
    public void testParameterized(PSInstance instance) {
        PSProblem problem = new PSProblem(instance);
        final PSRelax relax = new PSRelax(instance);
        final PSRanking ranking = new PSRanking();
        final FixedWidth<PSState> width = new FixedWidth<>(10);
        final VariableHeuristic<PSState> varh = new DefaultVariableHeuristic<>();
        final Frontier<PSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        assertEquals(solver.bestValue().get(), -instance.optimal); // put a minus since it is a minimization problem
    }
}