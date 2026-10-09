package org.ddolib.examples.layered.srflp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.ddolib.common.util.verbosity.VerbosityLevel;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.modeling.Problem;
import org.ddolib.layered.modeling.Relaxation;
import org.ddolib.layered.modeling.StateRanking;
import org.ddolib.layered.testbench.TestDataSupplier;

/**
 * Supplies the Single-Row Facility Layout Problem (SRFLP) instances (read from the files of a
 * directory) and the layered model used to solve them in the tests.
 */
public class SRFLPTestDataSupplier extends TestDataSupplier<SRFLPState, SRFLPProblem> {

    private final Path dir;

    /**
     * Creates a supplier reading the instances from the given directory.
     *
     * @param dir the directory containing the instance files
     */
    public SRFLPTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<SRFLPProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(
                            filePath -> {
                                try {
                                    return new SRFLPProblem(filePath.toString());
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
