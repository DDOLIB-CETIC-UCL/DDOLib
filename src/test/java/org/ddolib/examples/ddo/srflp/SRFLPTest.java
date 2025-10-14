package org.ddolib.examples.ddo.srflp;

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

public class SRFLPTest {


    private static class SRFLPBench extends ProblemTestBench<SRFLPState, NullType, SRFLPProblem> {

        @Override
        protected List<SRFLPProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "SRFLP").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);
            return stream.filter(file -> !file.isDirectory())
                    .map(file -> Paths.get(dir, file.getName()))
                    .map(filePath -> {
                        try {
                            return new SRFLPProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<SRFLPState, NullType> configSolver(SRFLPProblem problem) {
            SolverConfig<SRFLPState, NullType> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new SRFLPRelax(problem);
            config.flb = new SRFLPFastLowerBound(problem);
            config.ranking = new SRFLPRanking();
            config.width = new FixedWidth<>(maxWidth);
            config.varh = new DefaultVariableHeuristic<>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

            return config;
        }
    }

    @DisplayName("SRFLP")
    @TestFactory
    public Stream<DynamicTest> testSRFLP() {
        var bench = new SRFLPBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}
