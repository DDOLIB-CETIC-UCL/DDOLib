package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.examples.tsptw.*;
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

public class TSPTWTests {

    static Stream<TSPTWProblem> dataProvider() throws IOException {
        String dir = "src/test/resources/TSPTW/";

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> dir + fileName)
                .map(fileName -> {
                    try {
                        return TSPTW.readInstance(fileName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testFastUpperBoundAtRoot(TSPTWProblem problem) {
        final TSPTWRelax relax = new TSPTWRelax(problem);
        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        int rub = relax.fastUpperBound(problem.initialState(), vars);
        // Checks if the upper bound at the root is bigger than the optimal solution
        assertTrue(rub >= problem.optimal.get(),
                String.format("Upper bound %d is not bigger than the expected optimal solution %d",
                        rub,
                        problem.optimal.get()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testTSPTW(TSPTWProblem problem) {
        final TSPTWRelax relax = new TSPTWRelax(problem);
        final TSPTWRanking ranking = new TSPTWRanking();

        final FixedWidth<TSPTWState> width = new FixedWidth<>(500);
        final VariableHeuristic<TSPTWState> varh = new DefaultVariableHeuristic<>();
        final Frontier<TSPTWState> frontier = new SimpleFrontier<>(ranking);


        final ParallelSolver<TSPTWState> solver = new ParallelSolver<>(
                Runtime.getRuntime().availableProcessors(),
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
        );
        solver.maximize();

        assertEquals(solver.bestValue().get(), problem.optimal.get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testTSPTWWithRelax(TSPTWProblem problem) {
        final TSPTWRelax relax = new TSPTWRelax(problem);
        final TSPTWRanking ranking = new TSPTWRanking();

        final FixedWidth<TSPTWState> width = new FixedWidth<>(2);
        final VariableHeuristic<TSPTWState> varh = new DefaultVariableHeuristic<>();
        final Frontier<TSPTWState> frontier = new SimpleFrontier<>(ranking);


        final ParallelSolver<TSPTWState> solver = new ParallelSolver<>(
                Runtime.getRuntime().availableProcessors(),
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
        );
        solver.maximize();

        assertEquals(solver.bestValue().get(), problem.optimal.get());
    }


}
