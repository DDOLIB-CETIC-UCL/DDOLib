package org.ddolib.examples.layered.pdptw;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the Pickup and Delivery
 * Problem with Time Windows (PDPTW) instances.
 */
@Tag("non-regression")
public class PDPTWNonRegressionTest {

    /**
     * Generates one dynamic non-regression test per Pickup and Delivery Problem with Time Windows
     * (PDPTW) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("PDPTW: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionPDPTW() {
        var supplier =
                new PDPTWTestDataSupplier(
                        Path.of("src", "test", "resources", "Non-Regression", "PDPTW"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
