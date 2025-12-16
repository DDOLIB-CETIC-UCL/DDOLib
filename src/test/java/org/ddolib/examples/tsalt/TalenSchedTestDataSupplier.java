package org.ddolib.examples.tsalt;

import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.TestDataSupplier;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class TalenSchedTestDataSupplier extends TestDataSupplier<TSState, TSProblem> {

    private final Path dir;

    public TalenSchedTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<TSProblem> generateProblems() {

        try (Stream<Path> stream = Files.walk(dir)){
            return stream.filter(Files::isRegularFile)
                    .map(filePath -> {
                        try {
                            return new TSProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected DdoModel<TSState> model(TSProblem problem) {
        return new DdoModel<>() {

            @Override
            public Problem<TSState> problem() {
                return problem;
            }

            @Override
            public Relaxation<TSState> relaxation() {
                return new TSRelax(problem);
            }

            @Override
            public TSRanking ranking() {
                return new TSRanking();
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
        };
    }

}
