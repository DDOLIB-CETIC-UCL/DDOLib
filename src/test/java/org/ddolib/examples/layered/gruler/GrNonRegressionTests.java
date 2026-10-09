package org.ddolib.examples.layered.gruler;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the Golomb ruler instances.
 */
@Tag("non-regression")
public class GrNonRegressionTests {

    /**
     * Generates one dynamic non-regression test per Golomb ruler instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("G ruler: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonregressionGr() {
        var dataSupplier = new GrNonRegressionDataSupplier();
        var bench = new NonRegressionTestBench<>(dataSupplier);
        return bench.generateTests();
    }

    private static class GrNonRegressionDataSupplier extends GRTestDataSupplier {
        @Override
        protected List<GRProblem> generateProblems() {
            return IntStream.range(6, 10).mapToObj(GRProblem::new).toList();
        }
    }
}
