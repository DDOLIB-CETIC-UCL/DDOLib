package org.ddolib.examples.lcs;

import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.DebugLevel;
import org.ddolib.modeling.Problem;
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

public class LCSTest {
    private static class LCSBench extends ProblemTestBench<LCSState, LCSProblem> {

        public LCSBench() {
            super();
        }

        @Override
        protected List<LCSProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "LCS").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            return new LCSProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }


        @Override
        protected DdoModel<LCSState> model(LCSProblem problem) {
            return new DdoModel<>() {

                @Override
                public Problem<LCSState> problem() {
                    return problem;
                }

                @Override
                public LCSRelax relaxation() {
                    return new LCSRelax(problem);
                }

                @Override
                public LCSRanking ranking() {
                    return new LCSRanking();
                }

                @Override
                public LCSFastLowerBound lowerBound() {
                    return new LCSFastLowerBound(problem);
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

    @DisplayName("LCS")
    @TestFactory
    public Stream<DynamicTest> testLCS() {
        var bench = new LCSBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }


}
