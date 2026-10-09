package org.ddolib.examples.layered.gruler;

import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/** Tests of the layered solvers on Golomb ruler instances, using the {@link ProblemTestBench}. */
public class GRTest {

    /**
     * Generates the dynamic tests for each Golomb ruler instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("Golomb ruler")
    @TestFactory
    public Stream<DynamicTest> testGR() {
        var bench = new ProblemTestBench<>(new GRTestDataSupplier());
        bench.testRelaxation = true;
        return bench.generateTests();
    }
}
