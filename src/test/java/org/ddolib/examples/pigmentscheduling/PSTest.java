package org.ddolib.examples.pigmentscheduling;

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
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

class PSTest {

    private static class PSPBench extends ProblemTestBench<PSState, NullType, PSProblem> {

        public PSPBench() {
            super();
        }

        @Override
        protected List<PSProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "PSP", "2items").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        PSProblem problem = new PSProblem(new PSInstance(filePath.toString()));
                        problem.setName(filePath.getFileName().toString());
                        return problem;
                    }).toList();
        }

        @Override
        protected SolverConfig<PSState, NullType> configSolver(PSProblem problem) {
            SolverConfig<PSState, NullType> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new PSRelax(problem.instance);
            config.ranking = new PSRanking();
            config.fub = new PSFastUpperBound(problem.instance);
            config.width = new FixedWidth<>(maxWidth);
            config.varh = new DefaultVariableHeuristic<>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

            return config;
        }
    }

    @DisplayName("PSP")
    @TestFactory
    public Stream<DynamicTest> testPSP() {
        var bench = new PSPBench();
        bench.testRelaxation = true;
        bench.testFUB = true;
        return bench.generateTests();
    }
}