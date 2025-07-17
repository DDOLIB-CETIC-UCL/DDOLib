package org.ddolib.examples.ddo.max2sat;

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
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.ddolib.factory.Solvers.sequentialSolver;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Max2SatTest {

    static Stream<Max2SatProblem> dataProvider() throws IOException {
        String dir = Paths.get("src", "test", "resources", "Max2Sat").toString();

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        Max2SatProblem problem = Max2SatIO.readInstance(filePath.toString());
                        problem.setName(filePath.getFileName().toString());
                        return problem;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testFastUpperBound(Max2SatProblem problem) {
        final Max2SatFastUpperBound fub = new Max2SatFastUpperBound(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        double rub = fub.fastUpperBound(problem.initialState(), vars);

        assertTrue(rub >= problem.optimal.get(),
                String.format("Upper bound %f is not bigger or equal to the expected optimal " +
                                "solution %f",
                        rub,
                        problem.optimal.get()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testMax2Sat(Max2SatProblem problem) {
        Max2SatRelax relax = new Max2SatRelax(problem);
        Max2SatRanking ranking = new Max2SatRanking();

        final FixedWidth<Max2SatState> width = new FixedWidth<>(500);
        final VariableHeuristic<Max2SatState> varh = new DefaultVariableHeuristic<>();

        final Frontier<Max2SatState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
        );

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimal.get(), 1e-10);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testMax2SatWithRelax(Max2SatProblem problem) {
        Max2SatRelax relax = new Max2SatRelax(problem);
        Max2SatRanking ranking = new Max2SatRanking();

        final FixedWidth<Max2SatState> width = new FixedWidth<>(2);
        final VariableHeuristic<Max2SatState> varh = new DefaultVariableHeuristic<>();

        final Frontier<Max2SatState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
        );

        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimal.get(), 1e-10);
    }

}
