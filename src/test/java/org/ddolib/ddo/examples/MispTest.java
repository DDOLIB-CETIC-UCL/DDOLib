package org.ddolib.ddo.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.HashSet;
import java.util.stream.Stream;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.examples.Misp.*;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.ParallelSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MispTest {
    static Stream<MispProblem> dataProvider() throws IOException {
        String dir = "src/test/resources/MISP/";

        Stream<Path> stream = Files.list(Paths.get(dir));
        return stream.filter(file -> !Files.isDirectory(file))
                .map(Path::getFileName)
                .map(fileName -> dir + fileName.toString())
                .map(fileName -> {
                    try {
                        return Misp.readGraph(fileName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testFastUpperBound(MispProblem problem) {
        final MispRelax relax = new MispRelax(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        int rub = relax.fastUpperBound(problem.remainingNodes, vars);
        // Checks if the upper bound at the root is bigger than the optimal solution
        assertTrue(rub >= problem.optimal.get(),
                String.format("Upper bound %d is not bigger than the expected optimal solution %d",
                        rub,
                        problem.optimal.get()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testParameterized(MispProblem problem) {
        final MispRelax relax = new MispRelax(problem);
        final MispRanking ranking = new MispRanking();
        final FixedWidth<BitSet> width = new FixedWidth<>(250);
        final VariableHeuristic<BitSet> varh = new DefaultVariableHeuristic<BitSet>();

        final Frontier<BitSet> frontier = new SimpleFrontier<>(ranking);

        final Solver solver = new ParallelSolver<BitSet>(
                Runtime.getRuntime().availableProcessors(),
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);
        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.optimal.get());
    }


}
