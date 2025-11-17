package org.ddolib.examples.salbp1;

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
// solution =[3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,12,10,11,14,11]
public class SALBP1Test {
    private static class SALBP1Bench extends ProblemTestBench<SALBPState, SALBPProblem> {
        public SALBP1Bench() {super();}

        @Override
        protected List<SALBPProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "SALBP1").toString();
            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            return new SALBPProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected DdoModel<SALBPState> model(SALBPProblem problem) {
            return new DdoModel<SALBPState>() {
                @Override
                public Problem<SALBPState> problem() {
                    return problem;
                }
                @Override
                public SALBPRelax relaxation() {
                    return new SALBPRelax(problem);
                }

                @Override
                public SALBPRanking ranking() {
                    return new SALBPRanking();
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


    @DisplayName("SALBP1")
    @TestFactory
    public Stream<DynamicTest> testSALBP1() {
        var bench = new SALBP1Test.SALBP1Bench();
        bench.testRelaxation = true;
//        bench.testFLB = true;
//        bench.testDominance = true;
        return bench.generateTests();
    }
}
