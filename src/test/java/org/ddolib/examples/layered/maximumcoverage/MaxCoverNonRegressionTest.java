package org.ddolib.examples.layered.maximumcoverage;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the maximum coverage
 * instances.
 */
@Tag("non-regression")
public class MaxCoverNonRegressionTest {

    /**
     * Generates one dynamic non-regression test per maximum coverage instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("MaxCover: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionMaxCover() {
        Path dir = Path.of("src", "test", "resources", "Non-Regression", "MaxCover");
        var supplier = new MaxCoverNonRegressionTestDataSupplier(dir);
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
