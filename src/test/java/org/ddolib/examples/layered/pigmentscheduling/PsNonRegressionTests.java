package org.ddolib.examples.layered.pigmentscheduling;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the Pigment Sequencing
 * Problem (PSP) instances.
 */
@Tag("non-regression")
public class PsNonRegressionTests {
    /**
     * Generates one dynamic non-regression test per Pigment Sequencing Problem (PSP) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("PS: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionPs() {
        var supplier =
                new PSTestDataSupplier(
                        Path.of("src", "test", "resources", "Non-Regression", "PSP"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
