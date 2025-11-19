package org.ddolib.examples.tsalt;

import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
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

public class TalenSchedTest {

    private static class TSBench extends ProblemTestBench<TSState, TSProblem> {

        public TSBench() {
            super();
        }

        @Override
        protected List<TSProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "TalentScheduling").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);
            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            TSProblem problem = new TSProblem(filePath.toString());
                            problem.setName(filePath.getFileName().toString());
                            return problem;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected DdoModel<TSState> model(TSProblem problem) {
            return new DdoModel<>() {

                @Override
                public Problem<TSState> problem() {
                    return problem;
                }

                @Override
                public Relaxation<TSState> relaxation() {
                    return new TSRelax(problem);
                }

                @Override
                public TSRanking ranking() {
                    return new TSRanking();
                }

                @Override
                public TSFastLowerBound lowerBound() {
                    return new TSFastLowerBound(problem);
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

    @DisplayName("Talent Scheduling")
    @TestFactory
    public Stream<DynamicTest> testTS() {
        var bench = new TSBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }

}
