package org.ddolib.examples.srflp;

import org.ddolib.modeling.*;
import org.ddolib.util.testbench.TestDataSupplier;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class SRFLPTestDataSupplier extends TestDataSupplier<SRFLPState, SRFLPProblem> {

    private final String dir;

    public SRFLPTestDataSupplier(String dir) {
        this.dir = dir;
    }

    @Override
    protected List<SRFLPProblem> generateProblems() {
        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(file -> Paths.get(dir, file.getName()))
                .map(filePath -> {
                    try {
                        return new SRFLPProblem(filePath.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
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
