package org.ddolib.examples.smic;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
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

public class SMICTest {

    private static class SMICBench extends ProblemTestBench<SMICState, SMICProblem> {

        public SMICBench() {
            super();
        }

        @Override
        protected List<SMICProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "SMIC").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);
//            SMICProblem problem = new SMICProblem(filePath.toString());
            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            return new SMICProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected DdoModel<SMICState> model(SMICProblem problem) {
            return new DdoModel<>() {

                @Override
                public Problem<SMICState> problem() {
                    return problem;
                }

                @Override
                public SMICRelax relaxation() {
                    return new SMICRelax(problem);
                }

                @Override
                public SMICRanking ranking() {
                    return new SMICRanking();
                }

                @Override
                public SMICFastLowerBound lowerBound() {
                    return new SMICFastLowerBound(problem);
                }

                @Override
                public DominanceChecker<SMICState> dominance() {
                    return new SimpleDominanceChecker<>(new SMICDominance(), problem.nbVars());
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

    @DisplayName("SMIC")
    @TestFactory
    public Stream<DynamicTest> testSMIC() {
        var bench = new SMICBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testDominance = true;
        return bench.generateTests();
    }
}
