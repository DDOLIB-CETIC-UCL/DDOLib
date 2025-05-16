package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.examples.binpacking.*;
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

public class BinPackingTest {
    
    static Stream<BPPProblem> dataProvider() throws IOException {
        String dir = "src/test/resources/BinPacking/";

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> dir + fileName)
                    .map(fileName -> {
                        try {
                            return BPP.extractFile(fileName);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        @ParameterizedTest
        @MethodSource("dataProvider")
        public void testFastUpperBound(BPPProblem problem) {
            final BPPRelax relax = new BPPRelax(problem);

            HashSet<Integer> vars = new HashSet<>();
            for (int i = 0; i < problem.nbVars(); i++) {
                vars.add(i);
            }

            int rub = relax.fastUpperBound(problem.initialState(), vars);
            // Checks if the upper bound at the root is bigger than the optimal solution
            assertTrue(rub >= -problem.getOptimal().get(),
                    String.format("Upper bound %d is not bigger than the expected optimal solution %d",
                            rub,
                            problem.getOptimal().get()));
        }

        @ParameterizedTest
        @MethodSource("dataProvider")
        public void testLCS(BPPProblem problem) {
            final BPPRelax relax = new BPPRelax(problem);
            final BPPRanking ranking = new BPPRanking();
            final FixedWidth<BPPState> width = new FixedWidth<>(250);
            final VariableHeuristic<BPPState> varh = new DefaultVariableHeuristic<BPPState>();

            final Frontier<BPPState> frontier = new SimpleFrontier<>(ranking);

            final Solver solver = new ParallelSolver<BPPState>(
                    Runtime.getRuntime().availableProcessors(),
                    problem,
                    relax,
                    varh,
                    ranking,
                    width,
                    frontier);
            solver.maximize();
            assertEquals(-solver.bestValue().get(), problem.getOptimal().get());
        }

        @ParameterizedTest
        @MethodSource("dataProvider")
        public void testBPPWithRelax(BPPProblem problem) {
            final BPPRelax relax = new BPPRelax(problem);
            final BPPRanking ranking = new BPPRanking();
            final FixedWidth<BPPState> width = new FixedWidth<>(2);
            final VariableHeuristic<BPPState> varh = new DefaultVariableHeuristic<BPPState>();

            final Frontier<BPPState> frontier = new SimpleFrontier<>(ranking);

            final Solver solver = new ParallelSolver<BPPState>(
                    Runtime.getRuntime().availableProcessors(),
                    problem,
                    relax,
                    varh,
                    ranking,
                    width,
                    frontier);
            solver.maximize();
            assertEquals(-solver.bestValue().get(), problem.getOptimal().get());
        }
}
