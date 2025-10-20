package org.ddolib.examples.max2sat;

import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.DebugLevel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
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

public class Max2SatTest {

    private static class Max2SatBench extends ProblemTestBench<Max2SatState, Max2SatProblem> {

        @Override
        protected List<Max2SatProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "Max2Sat").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);
            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            return new Max2SatProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected DdoModel<Max2SatState> model(Max2SatProblem problem) {
            return new DdoModel<>() {

                @Override
                public Problem<Max2SatState> problem() {
                    return problem;
                }

                @Override
                public Relaxation<Max2SatState> relaxation() {
                    return new Max2SatRelax(problem);
                }

                @Override
                public Max2SatRanking ranking() {
                    return new Max2SatRanking();
                }

                @Override
                public Max2SatFastLowerBound lowerBound() {
                    return new Max2SatFastLowerBound(problem);
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

    @DisplayName("Max2Sat")
    @TestFactory
    public Stream<DynamicTest> testMax2Sat() {
        var bench = new Max2SatBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }


}
