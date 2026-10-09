package org.ddolib.examples.layered.pdp;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the Pickup and Delivery
 * Problem (PDP) instances.
 */
@Tag("non-regression")
public class PdpNonRegressionTests {

    /**
     * Generates one dynamic non-regression test per Pickup and Delivery Problem (PDP) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("PDP: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionPdp() {
        var supplier =
                new PDPTestDataSupplier(
                        Path.of("src", "test", "resources", "Non-Regression", "PDP"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
