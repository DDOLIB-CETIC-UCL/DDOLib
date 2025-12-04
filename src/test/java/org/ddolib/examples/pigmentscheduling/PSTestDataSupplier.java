package org.ddolib.examples.pigmentscheduling;

import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.util.testbench.TestDataSupplier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class PSTestDataSupplier extends TestDataSupplier<PSState, PSProblem> {

    private final Path dir;

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
