package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.examples.alp.*;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ALPTest {

    static Stream<ALPProblem> dataProvider() {
        String dir = "src/test/resources/ALP/";

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> dir + fileName)
                .map(fileName -> {
                    try {
                        return new ALPProblem(new ALPInstance(fileName));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testFastUpperBound(ALPProblem problem) {
        final ALPRelax relax = new ALPRelax(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        int rub = relax.fastUpperBound(problem.initialState(), vars);
        // Checks if the upper bound at the root is bigger than the optimal solution
        assertTrue(rub >= problem.getOptimal().get(),
                String.format("Upper bound %d is not bigger than the expected optimal solution %d",
                        rub,
                        problem.getOptimal().get()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testALP(ALPProblem problem) {
        final ALPRelax relax = new ALPRelax(problem);
        final ALPRanking ranking = new ALPRanking();
        final FixedWidth<ALPState> width = new FixedWidth<>(250);
        final VariableHeuristic<ALPState> varh = new DefaultVariableHeuristic<ALPState>();

        final Frontier<ALPState> frontier = new SimpleFrontier<>(ranking);

        final Solver solver = new ParallelSolver<ALPState>(
                Runtime.getRuntime().availableProcessors(),
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);
        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.getOptimal().get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testALPWithRelax(ALPProblem problem) {
        final ALPRelax relax = new ALPRelax(problem);
        final ALPRanking ranking = new ALPRanking();
        final FixedWidth<ALPState> width = new FixedWidth<>(100);
        final VariableHeuristic<ALPState> varh = new DefaultVariableHeuristic<ALPState>();

        final Frontier<ALPState> frontier = new SimpleFrontier<>(ranking);

        final Solver solver = new ParallelSolver<ALPState>(
                Runtime.getRuntime().availableProcessors(),
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);
        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.getOptimal().get());
    }
}
