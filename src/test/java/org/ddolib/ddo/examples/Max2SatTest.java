package org.ddolib.ddo.examples;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.examples.max2sat.*;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Max2SatTest {

    static Stream<Max2SatProblem> dataProvider() throws IOException {
        String dir = "src/test/resources/Max2Sat/";

        Stream<Path> stream = Files.list(Paths.get(dir));
        return stream.filter(file -> !Files.isDirectory(file))
                .map(Path::getFileName)
                .map(fileName -> dir + fileName.toString())
                .map(fileName -> {
                    try {
                        return Max2SatIO.readInstance(fileName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testFastUpperBound(Max2SatProblem problem) {
        final Max2SatRelax relax = new Max2SatRelax(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        int rub = relax.fastUpperBound(problem.initialState(), vars);

        assertTrue(rub >= problem.optimal.get(),
                String.format("Upper bound %d is not bigger than the expected optimal solution %d",
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

        final Frontier<Max2SatState> frontier = new SimpleFrontier<>(ranking);

        SequentialSolver<Max2SatState> solver = new SequentialSolver<>(
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
