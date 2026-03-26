package org.ddolib.examples.hrc;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.ExactModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.TestDataSupplier;
import org.ddolib.util.verbosity.VerbosityLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class HRCTestDataSupplier extends TestDataSupplier<HRCState, HRCProblem> {

    private final Path dir;

    public HRCTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<HRCProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile)
                    .map(filePath -> {
                        try {
                            return new HRCProblem(filePath.toString());
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
    protected DdoModel<HRCState> model(HRCProblem problem) {
        return new ExactModel<>() {
            @Override
            public Problem<HRCState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<HRCState> lowerBound() {
                return new HRCFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<HRCState> dominance() {
                return new SimpleDominanceChecker<>(new HRCDominance(), problem.nbVars());
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

