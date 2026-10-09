package org.ddolib.examples.layered.tsptw;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the Traveling Salesman
 * Problem with Time Windows (TSPTW) instances.
 */
@Tag("non-regression")
public class TsptwNonRegressionTests {

    /**
     * Generates one dynamic non-regression test per Traveling Salesman Problem with Time Windows
     * (TSPTW) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("TSPTW: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionTsptw() {
        var supplier =
                new TSPTWTestDataSupplier(
                        Path.of("src", "test", "resources", "Non-Regression", "TSPTW"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
