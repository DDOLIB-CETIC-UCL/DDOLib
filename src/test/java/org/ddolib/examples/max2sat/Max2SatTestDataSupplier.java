package org.ddolib.examples.max2sat;

import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.TestDataSupplier;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Max2SatTestDataSupplier extends TestDataSupplier<Max2SatState, Max2SatProblem> {

    private final Path dir;

    public Max2SatTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<Max2SatProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> {
                        try {
                            return new Max2SatProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected DdoModel<Max2SatState> model(Max2SatProblem problem) {
        return new DdoModel<>() {

            @Override
            public Problem<Max2SatState> problem() {
                return problem;
            }

            @Override
            public Max2SatFastLowerBound lowerBound() {
                return new Max2SatFastLowerBound(problem);
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }

            @Override
            public DebugLevel debugMode() {
                return DebugLevel.ON;
            }

            @Override
            public Relaxation<Max2SatState> relaxation() {
                return new Max2SatRelax(problem);
            }

            @Override
            public Max2SatRanking ranking() {
                return new Max2SatRanking();
            }
        };
    }
}
