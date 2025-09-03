package org.ddolib.examples.ddo.alp;


import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.ParallelSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.lang.model.type.NullType;
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
        final ALPFastUpperBound fub = new ALPFastUpperBound(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        double rub = fub.fastUpperBound(problem.initialState(), vars);
        // Checks if the upper bound at the root is bigger than the optimal solution
        assertTrue(rub >= problem.getOptimal().get(),
                String.format("Upper bound %.2f is not bigger than the expected optimal solution %.2f",
                        rub,
                        problem.getOptimal().get()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testALP(ALPProblem problem) {
        SolverConfig<ALPState, NullType> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new ALPRelax(problem);
        config.fub = new ALPFastUpperBound(problem);
        config.ranking = new ALPRanking();
        config.width = new FixedWidth<>(250);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

        final Solver solver = new ParallelSolver<>(config);
        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.getOptimal().get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testALPWithRelax(ALPProblem problem) {
        SolverConfig<ALPState, NullType> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new ALPRelax(problem);
        config.fub = new ALPFastUpperBound(problem);
        config.ranking = new ALPRanking();
        config.width = new FixedWidth<>(100);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

        final Solver solver = new ParallelSolver<>(config);
        solver.maximize();
        assertEquals(solver.bestValue().get(), problem.getOptimal().get());
    }
}
