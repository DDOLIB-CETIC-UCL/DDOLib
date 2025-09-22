package org.ddolib.examples.pdp;

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

public class PDPTests {

    private static class PDPBench extends ProblemTestBench<PDPState, NullType, PDPProblem> {

        public PDPBench() {
            super();
        }

        @Override
        protected List<PDPProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "PDP").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            PDPInstance instance = new PDPInstance(filePath.toString());
                            PDPProblem problem = new PDPProblem(instance);
                            problem.setName(filePath.getFileName().toString());
                            return problem;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<PDPState, NullType> configSolver(PDPProblem problem) {
            SolverConfig<PDPState, NullType> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new PDPRelax(problem);
            config.ranking = new PDPRanking();
            config.flb = new PDPFastLowerBound(problem);
            config.width = new FixedWidth<>(maxWidth);
            config.varh = new DefaultVariableHeuristic<>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.Frontier);

            return config;
        }
    }

    @DisplayName("PDP")
    @TestFactory
    public Stream<DynamicTest> testPDP() {
        var bench = new PDPBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.minWidth = 45;
        bench.maxWidth = 50;
        return bench.generateTests();
    }
}
