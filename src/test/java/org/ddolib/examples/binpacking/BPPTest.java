package org.ddolib.examples.binpacking;

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

public class BPPTest {
    private static class BPPBench extends ProblemTestBench<BPPState, NullType, BPPProblem> {

        public BPPBench() {
            super();
        }

        @Override
        protected List<BPPProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "BinPacking").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            BPPProblem problem = BPP.extractFile(filePath.toString());
                            problem.setName(filePath.getFileName().toString());
                            return problem;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList().subList(0,1);
        }

        @Override
        protected SolverConfig<BPPState, NullType> configSolver(BPPProblem problem) {
            SolverConfig<BPPState, NullType> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new BPPRelax(problem);
            config.ranking = new BPPRanking();
            config.flb = new BPPFastLowerBound(problem);

            config.width = new FixedWidth<>(maxWidth);
            config.varh = new DefaultVariableHeuristic<>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
            System.out.println("config: " + config);
            return config;
        }
    }

    @DisplayName("BPP")
    @TestFactory
    public Stream<DynamicTest> testLCS() {
        var bench = new BPPTest.BPPBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }


}
