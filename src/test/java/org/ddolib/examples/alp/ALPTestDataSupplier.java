package org.ddolib.examples.alp;

import org.ddolib.modeling.DdoModel;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.TestDataSupplier;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class ALPTestDataSupplier extends TestDataSupplier<ALPState, ALPProblem> {


    private final Path dir;

    public ALPTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<ALPProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> {
                        try {
                            return new ALPProblem(filePath.toString());
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
    protected DdoModel<ALPState> model(ALPProblem problem) {
        return new DdoModel<>() {
            @Override
            public ALPProblem problem() {
                return problem;
            }

            @Override
            public ALPFastLowerBound lowerBound() {
                return new ALPFastLowerBound(problem);
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
            public ALPRelax relaxation() {
                return new ALPRelax(problem);
            }

            @Override
            public ALPRanking ranking() {
                return new ALPRanking();
            }
        };
    }
}
