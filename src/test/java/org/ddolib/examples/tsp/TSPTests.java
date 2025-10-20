package org.ddolib.examples.tsp;

import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class TSPTests {

    private static class TSPBench extends ProblemTestBench<TSPState, TSPProblem> {

        public TSPBench() {
            super();
        }

        @Override
        protected List<TSPProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "TSP").toString();
            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            return new TSPProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected DdoModel<TSPState> model(TSPProblem problem) {
            return new DdoModel<>() {

                @Override
                public Problem<TSPState> problem() {
                    return problem;
                }

                @Override
                public Relaxation<TSPState> relaxation() {
                    return new TSPRelax(problem);
                }

                @Override
                public TSPRanking ranking() {
                    return new TSPRanking();
                }

                @Override
                public TSPFastLowerBound lowerBound() {
                    return new TSPFastLowerBound(problem);
                }

                @Override
                public boolean useCache() {
                    return true;
                }

                @Override
                public WidthHeuristic<TSPState> widthHeuristic() {
                    return new FixedWidth<>(500);
                }
            };
        }
    }

    @DisplayName("TSP")
    @TestFactory
    public Stream<DynamicTest> testTSP() {
        var bench = new TSPBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}
