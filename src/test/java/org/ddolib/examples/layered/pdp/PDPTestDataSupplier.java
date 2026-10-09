package org.ddolib.examples.layered.pdp;

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
 * Supplies the Pickup and Delivery Problem (PDP) instances (read from the files of a directory) and
 * the layered model used to solve them in the tests.
 */
public class PDPTestDataSupplier extends TestDataSupplier<PDPState, PDPProblem> {

    private final Path dir;

    /**
     * Creates a supplier reading the instances from the given directory.
     *
     * @param dir the directory containing the instance files
     */
    public PDPTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<PDPProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(
                            filePath -> {
                                try {
                                    return new PDPProblem(filePath.toString());
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
