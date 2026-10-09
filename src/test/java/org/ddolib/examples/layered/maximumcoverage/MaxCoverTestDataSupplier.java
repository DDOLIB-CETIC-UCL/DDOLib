package org.ddolib.examples.layered.maximumcoverage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.ddolib.common.util.debug.DebugLevel;
import org.ddolib.common.util.verbosity.VerbosityLevel;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.DefaultDominanceChecker;
import org.ddolib.layered.modeling.DominanceChecker;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.testbench.TestDataSupplier;

/**
 * Supplies the maximum coverage instances (read from the files of a directory) and the layered
 * model used to solve them in the tests.
 */
public class MaxCoverTestDataSupplier extends TestDataSupplier<MaxCoverState, MaxCoverProblem> {

    private final Path dir;

    /**
     * Creates a supplier reading the instances from the given directory.
     *
     * @param dir the directory containing the instance files
     */
    public MaxCoverTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<MaxCoverProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile)
                    .map(
                            filePath -> {
                                try {
                                    return new MaxCoverProblem(filePath.toString());
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
    protected DdoModel<MaxCoverState> model(MaxCoverProblem problem) {
        return new DdoModel<>() {

            @Override
            public Problem<MaxCoverState> problem() {
                return problem;
            }

            @Override
            public Relaxation<MaxCoverState> relaxation() {
                return new MaxCoverRelax(problem);
            }

            @Override
            public MaxCoverRanking ranking() {
                return new MaxCoverRanking();
            }

            @Override
            public FastLowerBound<MaxCoverState> lowerBound() {
                return new MaxCoverFastLowerBound(problem);
            }

            @Override
            public DominanceChecker<MaxCoverState> dominance() {
                return new DefaultDominanceChecker<>();
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
