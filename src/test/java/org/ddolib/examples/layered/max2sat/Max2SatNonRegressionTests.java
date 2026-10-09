package org.ddolib.examples.layered.max2sat;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/** Non-regression tests checking that all the layered solvers agree on the Max2Sat instances. */
@Tag("non-regression")
public class Max2SatNonRegressionTests {

    /**
     * Generates one dynamic non-regression test per Max2Sat instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("Max2Sat: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionMax2Sat() {
        var supplier =
                new Max2SatTestDataSupplier(
                        Path.of("src", "test", "resources", "Non-Regression", "Max2Sat"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
