package org.ddolib.examples.pdp;

import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.TestUnit;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class PDPTestUnit extends TestUnit<PDPState, PDPProblem> {


    private final String dir;

    public PDPTestUnit(String dir) {
        this.dir = dir;
    }

    @Override
    protected List<PDPProblem> generateProblems() {
        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);

        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        return new PDPProblem(filePath.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    @Override
    protected DdoModel<PDPState> model(PDPProblem problem) {
        return new DdoModel<>() {
            @Override
            public Problem<PDPState> problem() {
                return problem;
            }

            @Override
            public PDPFastLowerBound lowerBound() {
                return new PDPFastLowerBound(problem);
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
            public PDPRelax relaxation() {
                return new PDPRelax(problem);
            }

            @Override
            public PDPRanking ranking() {
                return new PDPRanking();
            }
        };
    }
}
