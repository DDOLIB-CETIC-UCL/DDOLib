package org.ddolib.examples.ddo.tstptw;

import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.examples.ddo.tsptw.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.Stream;

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
        final TSPTWFastUpperBound fub = new TSPTWFastUpperBound(problem);
        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        double rub = fub.fastUpperBound(problem.initialState(), vars);
        // Checks if the upper bound at the root is bigger than the optimal solution
        assertTrue(rub >= instance.optimal.get(),
                String.format("Upper bound %.1f is not bigger than the expected optimal solution %.1f",
                        rub,
                        instance.optimal.get()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testTSPTW(TSPTWInstance instance) {
        SolverConfig<TSPTWState, TSPTWDominanceKey> config = new SolverConfig<>();
        final TSPTWProblem problem = new TSPTWProblem(instance);
        config.problem = problem;
        config.relax = new TSPTWRelax(problem);
        config.ranking = new TSPTWRanking();
        config.fub = new TSPTWFastUpperBound(problem);

        config.width = new FixedWidth<>(50);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);


        final Solver solver = new SequentialSolver<>(config);
        solver.maximize();

        assertEquals(solver.bestValue().get(), instance.optimal.get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testTSPTWWithRelax(TSPTWInstance instance) {
        SolverConfig<TSPTWState, TSPTWDominanceKey> config = new SolverConfig<>();
        final TSPTWProblem problem = new TSPTWProblem(instance);
        config.problem = problem;
        config.relax = new TSPTWRelax(problem);
        config.ranking = new TSPTWRanking();
        config.fub = new TSPTWFastUpperBound(problem);

        config.width = new FixedWidth<>(2);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);


        final Solver solver = new SequentialSolver<>(config);
        solver.maximize();

        assertEquals(solver.bestValue().get(), instance.optimal.get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testTSPTWWithDominance(TSPTWInstance instance) {
        SolverConfig<TSPTWState, TSPTWDominanceKey> config = new SolverConfig<>();
        final TSPTWProblem problem = new TSPTWProblem(instance);
        config.problem = problem;
        config.relax = new TSPTWRelax(problem);
        config.ranking = new TSPTWRanking();

        config.width = new FixedWidth<>(50);
        config.varh = new DefaultVariableHeuristic<>();
        config.dominance = new SimpleDominanceChecker<>(new TSPTWDominance(), problem.nbVars());
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);


        final Solver solver = new SequentialSolver<>(config);
        solver.maximize();
        solver.maximize();

        assertEquals(solver.bestValue().get(), instance.optimal.get());
    }


}
