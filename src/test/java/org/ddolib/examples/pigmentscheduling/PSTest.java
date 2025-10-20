package org.ddolib.examples.pigmentscheduling;

import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.DebugLevel;
import org.ddolib.modeling.VerbosityLevel;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

class PSTest {

    private static class PSPBench extends ProblemTestBench<PSState, PSProblem> {

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
        protected DdoModel<PSState> model(PSProblem problem) {
            return new DdoModel<>() {

                @Override
                public PSProblem problem() {
                    return problem;
                }

                @Override
                public PSRelax relaxation() {
                    return new PSRelax(problem.instance);
                }

                @Override
                public PSRanking ranking() {
                    return new PSRanking();
                }

                @Override
                public PSFastLowerBound lowerBound() {
                    return new PSFastLowerBound(problem.instance);
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

    @DisplayName("PSP")
    @TestFactory
    public Stream<DynamicTest> testPSP() {
        var bench = new PSPBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}