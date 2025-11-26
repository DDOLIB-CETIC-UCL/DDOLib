package org.ddolib.examples.talentscheduling;

import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.TestUnit;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class TalentSchedTestUnit extends TestUnit<TSState, TSProblem> {

    private final String dir;

    public TalentSchedTestUnit(String dir) {
        this.dir = dir;
    }

    @Override
    protected List<TSProblem> generateProblems() {
        File[] files = new File(dir).listFiles();
        assert files != null;
        Stream<File> stream = Stream.of(files);
        return stream.filter(file -> !file.isDirectory())
                .map(File::getName)
                .map(fileName -> Paths.get(dir, fileName))
                .map(filePath -> {
                    try {
                        return new TSProblem(filePath.toString());
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

            @Override
            public Relaxation<TSState> relaxation() {
                return new TSRelax(problem);
            }

            @Override
            public TSRanking ranking() {
                return new TSRanking();
            }
        };
    }
}
