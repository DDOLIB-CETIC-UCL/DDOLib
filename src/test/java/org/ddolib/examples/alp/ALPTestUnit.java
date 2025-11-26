package org.ddolib.examples.alp;

import org.ddolib.modeling.DdoModel;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.TestUnit;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class ALPTestUnit extends TestUnit<ALPState, ALPProblem> {


    private final String dir;

    public ALPTestUnit(String dir) {
        this.dir = dir;
    }

    @Override
    protected List<ALPProblem> generateProblems() {
        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);

        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        return new ALPProblem(filePath.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
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
