package org.ddolib.examples.layered.pdptw;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the layered solvers on Pickup and Delivery Problem with Time Windows (PDPTW) instances,
 * using the {@link ProblemTestBench}.
 */
public class PDPTWTest {
    // TODO: use instances with known solution
    /**
     * Generates the dynamic tests for each Pickup and Delivery Problem with Time Windows (PDPTW)
     * instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("PDPTW")
    @TestFactory
    public Stream<DynamicTest> testPDPTW() {
        var dataSupplier = new PDPTWTestDataSupplier(Path.of("src", "test", "resources", "PDPTW"));

        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.minWidth = 45;
        bench.maxWidth = 50;
        return bench.generateTests();
    }
}
