package org.ddolib.examples.layered.pigmentscheduling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.ddolib.layered.modeling.DdoModel;
import org.ddolib.layered.modeling.FastLowerBound;
import org.ddolib.layered.testbench.TestDataSupplier;

/**
 * Supplies the Pigment Sequencing Problem (PSP) instances (read from the files of a directory) and
 * the layered model used to solve them in the tests.
 */
public class PSTestDataSupplier extends TestDataSupplier<PSState, PSProblem> {

    private final Path dir;

    /**
     * Creates a supplier reading the instances from the given directory.
     *
     * @param dir the directory containing the instance files
     */
    public PSTestDataSupplier(Path dir) {
        this.dir = dir;
    }

    @Override
    protected List<PSProblem> generateProblems() {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile) // get only files
                    .map(filePath -> new PSProblem(filePath.toString()))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected DdoModel<PSState> model(PSProblem problem) {
        return new DdoModel<>() {

            @Override
            public PSProblem problem() {
                return problem;
            }

            @Override
            public FastLowerBound<PSState> lowerBound() {
                return new PSFastLowerBound(problem);
            }

            @Override
            public PSRelax relaxation() {
                return new PSRelax(problem);
            }

            @Override
            public PSRanking ranking() {
                return new PSRanking();
            }
        };
    }
}
