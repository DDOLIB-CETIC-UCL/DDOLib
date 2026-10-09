package org.ddolib.examples.layered.smic;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the Single Machine with
 * Inventory Constraint (SMIC) instances.
 */
@Tag("non-regression")
public class SmicNonRegressionTests {

    /**
     * Generates one dynamic non-regression test per Single Machine with Inventory Constraint (SMIC)
     * instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("SMIC: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionSmic() {
        var supplier =
                new SMICTestDataSupplier(
                        Path.of("src", "test", "resources", "Non-Regression", "SMIC"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
