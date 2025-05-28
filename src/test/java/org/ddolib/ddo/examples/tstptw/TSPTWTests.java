package org.ddolib.ddo.examples.tstptw;

import org.ddolib.ddo.core.Frontier;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.examples.tsptw.*;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TSPTWTests {

    static Stream<TSPTWInstance> dataProvider() throws IOException {
        String dir = Paths.get("src", "test", "resources", "TSPTW").toString();

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        return new TSPTWInstance(filePath.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testFastUpperBoundAtRoot(TSPTWInstance instance) {
        final TSPTWProblem problem = new TSPTWProblem(instance);
        final TSPTWRelax relax = new TSPTWRelax(problem);
        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        int rub = relax.fastUpperBound(problem.initialState(), vars);
        // Checks if the upper bound at the root is bigger than the optimal solution
        assertTrue(rub >= instance.optimal.get(),
                String.format("Upper bound %d is not bigger than the expected optimal solution %d",
                        rub,
                        instance.optimal.get()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testTSPTW(TSPTWInstance instance) {
        final TSPTWProblem problem = new TSPTWProblem(instance);
        final TSPTWRelax relax = new TSPTWRelax(problem);
        final TSPTWRanking ranking = new TSPTWRanking();

        final FixedWidth<TSPTWState> width = new FixedWidth<>(50);
        final VariableHeuristic<TSPTWState> varh = new DefaultVariableHeuristic<>();
        final Frontier<TSPTWState> frontier = new SimpleFrontier<>(ranking);


        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
        );
        solver.maximize();

        assertEquals(solver.bestValue().get(), instance.optimal.get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testTSPTWWithRelax(TSPTWInstance instance) {
        final TSPTWProblem problem = new TSPTWProblem(instance);
        final TSPTWRelax relax = new TSPTWRelax(problem);
        final TSPTWRanking ranking = new TSPTWRanking();

        final FixedWidth<TSPTWState> width = new FixedWidth<>(2);
        final VariableHeuristic<TSPTWState> varh = new DefaultVariableHeuristic<>();
        final Frontier<TSPTWState> frontier = new SimpleFrontier<>(ranking);


        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier
        );
        solver.maximize();

        assertEquals(solver.bestValue().get(), instance.optimal.get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testTSPTWWithDominance(TSPTWInstance instance) {
        final TSPTWProblem problem = new TSPTWProblem(instance);
        final TSPTWRelax relax = new TSPTWRelax(problem);
        final TSPTWRanking ranking = new TSPTWRanking();

        final FixedWidth<TSPTWState> width = new FixedWidth<>(2);
        final VariableHeuristic<TSPTWState> varh = new DefaultVariableHeuristic<>();
        final SimpleDominanceChecker<TSPTWState, TSPTWDominanceKey> dominance =
                new SimpleDominanceChecker<>(new TSPTWDominance(), problem.nbVars());
        final Frontier<TSPTWState> frontier = new SimpleFrontier<>(ranking);


        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                dominance
        );
        solver.maximize();

        assertEquals(solver.bestValue().get(), instance.optimal.get());
    }


}
