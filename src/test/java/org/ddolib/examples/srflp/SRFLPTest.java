package org.ddolib.examples.srflp;

import org.ddolib.modeling.*;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class SRFLPTest {


    private static class SRFLPBench extends ProblemTestBench<SRFLPState, SRFLPProblem> {

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
        protected DdoModel<SRFLPState> model(SRFLPProblem problem) {
            return new DdoModel<>() {
                @Override
                public Problem<SRFLPState> problem() {
                    return problem;
                }

                @Override
                public Relaxation<SRFLPState> relaxation() {
                    return new SRFLPRelax(problem);
                }

                @Override
                public StateRanking<SRFLPState> ranking() {
                    return new SRFLPRanking();
                }

                @Override
                public FastLowerBound<SRFLPState> lowerBound() {
                    return new SRFLPFastLowerBound(problem);
                }

                @Override
                public VerbosityLevel verbosityLevel() {
                    return VerbosityLevel.SILENT;
                }
            };
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
