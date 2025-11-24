package org.ddolib.examples.binpacking;

import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.modeling.*;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class BPPTest {
    private static class BPPBench extends ProblemTestBench<BPPState, BPPProblem> {

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
        protected DdoModel<BPPState> model(BPPProblem problem) {
            return new DdoModel<>() {
                private final BPPRanking ranking = new BPPRanking();

                @Override
                public Relaxation<BPPState> relaxation() {
                    return new BPPRelax(problem) {};
                }

                @Override
                public Problem<BPPState> problem() {
                    return problem;
                }

                @Override
                public FastLowerBound<BPPState> lowerBound() {
                    return new BPPFastLowerBound(problem);
                }

                @Override
                public StateRanking<BPPState> ranking() {
                    return ranking;
                }

                @Override
                public Frontier<BPPState> frontier() {
                    return new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
                }

                @Override
                public DebugLevel debugMode() {
                    return DebugLevel.ON;
                }
            };
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
