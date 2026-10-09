package org.ddolib.examples.layered.lcs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.ddolib.common.util.debug.DebugLevel;
import org.ddolib.common.util.verbosity.VerbosityLevel;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.testbench.TestDataSupplier;

/**
 * Supplies the Longest Common Subsequence (LCS) instances (read from the files of a directory) and
 * the layered model used to solve them in the tests.
 */
public class LCSTestDataSupplier extends TestDataSupplier<LCSState, LCSProblem> {

    private final Path dir;

    /**
     * Creates a supplier reading the instances from the given directory.
     *
     * @param dir the directory containing the instance files
     */
    public LCSTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<LCSProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(
                            filePath -> {
                                try {
                                    return new LCSProblem(filePath.toString());
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
    protected DdoModel<LCSState> model(LCSProblem problem) {
        return new DdoModel<>() {

            @Override
            public Problem<LCSState> problem() {
                return problem;
            }

            @Override
            public LCSFastLowerBound lowerBound() {
                return new LCSFastLowerBound(problem);
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
            public LCSRelax relaxation() {
                return new LCSRelax(problem);
            }

            @Override
            public LCSRanking ranking() {
                return new LCSRanking();
            }
        };
    }
}
