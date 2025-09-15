package org.ddolib.examples.ddo.alp;


import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.lang.model.type.NullType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class ALPTest {

    public static class AlpBench extends ProblemTestBench<ALPState, NullType, ALPProblem> {

        @Override
        protected List<ALPProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "ALP").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            ALPInstance instance = new ALPInstance(filePath.toString());
                            ALPProblem problem = new ALPProblem(instance);
                            problem.setName(filePath.getFileName().toString());
                            return problem;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<ALPState, NullType> configSolver(ALPProblem problem) {
            SolverConfig<ALPState, NullType> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new ALPRelax(problem);
            config.ranking = new ALPRanking();
            config.fub = new ALPFastUpperBound(problem);

            config.width = new FixedWidth<>(100);
            config.varh = new DefaultVariableHeuristic<>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);


            return config;
        }

    }

    @DisplayName("ALP")
    @TestFactory
    public Stream<DynamicTest> testALP() {
        var bench = new ALPTest.AlpBench();
        bench.testRelaxation = true;
        bench.testFUB = true;
        return bench.generateTests();
    }
}
