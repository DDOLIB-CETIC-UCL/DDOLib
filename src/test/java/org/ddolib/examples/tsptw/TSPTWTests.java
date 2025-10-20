package org.ddolib.examples.tsptw;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.DebugLevel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.VerbosityLevel;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class TSPTWTests {

    private static class TSPTWBench extends ProblemTestBench<TSPTWState, TSPTWProblem> {

        public TSPTWBench() {
            super();
        }

        @Override
        protected List<TSPTWProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "TSPTW").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);
            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            return new TSPTWProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected DdoModel<TSPTWState> model(TSPTWProblem problem) {
            return new DdoModel<>() {

                @Override
                public Problem<TSPTWState> problem() {
                    return problem;
                }

                @Override
                public TSPTWRelax relaxation() {
                    return new TSPTWRelax(problem);
                }

                @Override
                public TSPTWRanking ranking() {
                    return new TSPTWRanking();
                }

                @Override
                public TSPTWFastLowerBound lowerBound() {
                    return new TSPTWFastLowerBound(problem);
                }

                @Override
                public DominanceChecker<TSPTWState> dominance() {
                    return new SimpleDominanceChecker<>(new TSPTWDominance(), problem.nbVars());
                }

                @Override
                public Frontier<TSPTWState> frontier() {
                    return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
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

    @DisplayName("TSPTW")
    @TestFactory
    public Stream<DynamicTest> testTSPTW() {
        var bench = new TSPTWBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testDominance = true;
        bench.testCache = true;
        return bench.generateTests();
    }
}
