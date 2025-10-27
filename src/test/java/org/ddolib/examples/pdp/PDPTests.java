package org.ddolib.examples.pdp;

import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
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

public class PDPTests {

    private static class PDPBench extends ProblemTestBench<PDPState, PDPProblem> {

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
                            return new PDPProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected DdoModel<PDPState> model(PDPProblem problem) {
            return new DdoModel<>() {
                @Override
                public Problem<PDPState> problem() {
                    return problem;
                }

                @Override
                public PDPRelax relaxation() {
                    return new PDPRelax(problem);
                }

                @Override
                public PDPRanking ranking() {
                    return new PDPRanking();
                }

                @Override
                public PDPFastLowerBound lowerBound() {
                    return new PDPFastLowerBound(problem);
                }

                @Override
                public DebugLevel debugMode() {
                    return DebugLevel.ON;
                }

                @Override
                public VerbosityLevel verbosityLevel() {
                    return VerbosityLevel.SILENT;
                }
            };
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
