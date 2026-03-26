package org.ddolib.examples.hrcp;

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

public class HRCPTestDataSupplier extends TestDataSupplier<HRCPState, HRCPProblem> {

    private final Path dir;

    public HRCPTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<HRCPProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile)
                    .map(filePath -> {
                        try {
                            return new HRCPProblem(filePath.toString());
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
    protected DdoModel<HRCPState> model(HRCPProblem problem) {
        return new ExactModel<>() {
            @Override
            public Problem<HRCPState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<HRCPState> lowerBound() {
                return new HRCPFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<HRCPState> dominance() {
                return new SimpleDominanceChecker<>(new HRCPDominance(), problem.nbVars());
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

