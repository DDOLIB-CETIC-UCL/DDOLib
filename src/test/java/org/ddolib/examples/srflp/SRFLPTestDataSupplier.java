package org.ddolib.examples.srflp;

import org.ddolib.modeling.*;
import org.ddolib.util.testbench.TestDataSupplier;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class SRFLPTestDataSupplier extends TestDataSupplier<SRFLPState, SRFLPProblem> {

    private final Path dir;

    public SRFLPTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<SRFLPProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> {
                        try {
                            return new SRFLPProblem(filePath.toString());
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
    protected DdoModel<SRFLPState> model(SRFLPProblem problem) {
        return new DdoModel<>() {
            @Override
            public Problem<SRFLPState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<SRFLPState> lowerBound() {
                return new SRFLPFastLowerBound(problem);
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }

            @Override
            public Relaxation<SRFLPState> relaxation() {
                return new SRFLPRelax(problem);
            }

            @Override
            public StateRanking<SRFLPState> ranking() {
                return new SRFLPRanking();
            }
        };
    }
}
