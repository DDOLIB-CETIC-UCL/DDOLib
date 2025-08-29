package org.ddolib.examples.ddo.srflp;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.lang.model.type.NullType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SRFLPTest {

    static Stream<SRFLPProblem> dataProvider() {
        String dir = Paths.get("src", "test", "resources", "SRFLP").toString();

        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(file -> Paths.get(dir, file.getName()))
                .map(filePath -> {
                    try {
                        SRFLPProblem problem = SRFLPIO.readInstance(filePath.toString());
                        problem.setName(filePath.getFileName().toString());
                        return problem;

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testSRFLP(SRFLPProblem problem) {
        SolverConfig<SRFLPState, NullType> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new SRFLPRelax(problem);
        config.ranking = new SRFLPRanking();

        config.width = new FixedWidth<>(1000);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

        Solver solver = new SequentialSolver<>(config);

        solver.maximize();
        assertEquals(problem.optimal.get(), -solver.bestValue().get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testFastLowerBoundAtRoot(SRFLPProblem problem) {
        final SRFLPFastUpperBound fub = new SRFLPFastUpperBound(problem);
        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        double rub = fub.fastUpperBound(problem.initialState(), vars);
        assertTrue(-rub <= problem.optimal.get(),
                String.format("Lower bound %f is not smaller than the expected optimal solution %f",
                        rub,
                        problem.optimal.get()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testSRFLPWithRelaxation(SRFLPProblem problem) {
        SolverConfig<SRFLPState, NullType> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new SRFLPRelax(problem);
        config.ranking = new SRFLPRanking();

        config.width = new FixedWidth<>(1000);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

        Solver solver = new SequentialSolver<>(config);


        solver.maximize();
        assertEquals(problem.optimal.get(), -solver.bestValue().get());
    }


}
