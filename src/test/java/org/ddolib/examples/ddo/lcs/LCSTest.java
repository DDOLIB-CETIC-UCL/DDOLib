package org.ddolib.examples.ddo.lcs;

import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.ddolib.factory.Solvers.sequentialSolver;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LCSTest {

    static Stream<LCSProblem> dataProvider() throws IOException {
        String dir = "src/test/resources/LCS/";

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> dir + fileName)
                .map(fileName -> {
                    try {
                        return LCSMain.extractFile(fileName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testFastUpperBound(LCSProblem problem) {
        final LCSFastUpperBound fub = new LCSFastUpperBound(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        double rub = fub.fastUpperBound(problem.initialState(), vars);
        // Checks if the upper bound at the root is bigger than the optimal solution
        assertTrue(rub >= problem.getOptimal().get(),
                String.format("Upper bound %.1f is not bigger than the expected optimal solution %.1f",
                        rub,
                        problem.getOptimal().get()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testLCS(LCSProblem problem) {
        final LCSRelax relax = new LCSRelax(problem);
        final LCSRanking ranking = new LCSRanking();
        final FixedWidth<LCSState> width = new FixedWidth<>(250);
        final VariableHeuristic<LCSState> varh = new DefaultVariableHeuristic<LCSState>();

        final Frontier<LCSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        final Solver solver = sequentialSolver(
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
    public void testLCSWithRelax(LCSProblem problem) {
        final LCSRelax relax = new LCSRelax(problem);
        final LCSRanking ranking = new LCSRanking();
        final FixedWidth<LCSState> width = new FixedWidth<>(2);
        final VariableHeuristic<LCSState> varh = new DefaultVariableHeuristic<LCSState>();

        final Frontier<LCSState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        final Solver solver = sequentialSolver(
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
