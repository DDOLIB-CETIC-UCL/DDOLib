package org.ddolib.examples.maximumcoverage;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.examples.knapsack.KSProblem;
import org.ddolib.modeling.*;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.ProblemTestBench;
import org.ddolib.util.verbosity.VerbosityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class MaxCoverTest {
    private static class MaxCoverBench extends ProblemTestBench<MaxCoverState, MaxCoverProblem> {

        public MaxCoverBench() {
            super();
        }

        @Override
        protected List<MaxCoverProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "MaxCover").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            return new MaxCoverProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected DdoModel<MaxCoverState> model(MaxCoverProblem problem) {
            return new DdoModel<>() {

                @Override
                public Problem<MaxCoverState> problem() {
                    return problem;
                }

                @Override
                public Relaxation<MaxCoverState> relaxation() {
                    return new MaxCoverRelax(problem);
                }

                @Override
                public MaxCoverRanking ranking() {
                    return new MaxCoverRanking();
                }

                @Override
                public FastLowerBound<MaxCoverState> lowerBound() {
                    return new DefaultFastLowerBound<>();
                }

                @Override
                public DominanceChecker<MaxCoverState> dominance() {
                    return new DefaultDominanceChecker<>();
                }

                @Override
                public VerbosityLevel verbosityLevel() {
                    return VerbosityLevel.SILENT;
                }

                @Override
                public DebugLevel debugMode() {
                    return DebugLevel.ON;
                }
            };
        }
    }

    @DisplayName("MaxCover")
    @TestFactory
    public Stream<DynamicTest> testMaxCover() {
        var bench = new MaxCoverBench();
        bench.testRelaxation = true;
        bench.testFLB = false;
        bench.testDominance = false;
        bench.testCache = true;
        return bench.generateTests();
    }
}
