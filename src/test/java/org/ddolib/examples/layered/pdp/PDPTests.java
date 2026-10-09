package org.ddolib.examples.layered.pdp;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the layered solvers on Pickup and Delivery Problem (PDP) instances, using the {@link
 * ProblemTestBench}.
 */
public class PDPTests {

    /**
     * Generates the dynamic tests for each Pickup and Delivery Problem (PDP) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("PDP")
    @TestFactory
    public Stream<DynamicTest> testPDP() {
        var dataSupplier = new PDPTestDataSupplier(Path.of("src", "test", "resources", "PDP"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.minWidth = 45;
        bench.maxWidth = 50;
        return bench.generateTests();
    }
}
