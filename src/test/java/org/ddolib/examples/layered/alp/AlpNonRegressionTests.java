package org.ddolib.examples.layered.alp;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the Aircraft Landing Problem
 * (ALP) instances.
 */
@Tag("non-regression")
public class AlpNonRegressionTests {

    /**
     * Generates one dynamic non-regression test per Aircraft Landing Problem (ALP) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("ALP: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionAlp() {
        var supplier =
                new ALPTestDataSupplier(
                        Path.of("src", "test", "resources", "Non-Regression", "ALP"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
