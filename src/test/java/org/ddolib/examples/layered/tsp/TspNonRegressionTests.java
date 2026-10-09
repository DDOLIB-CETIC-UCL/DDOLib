package org.ddolib.examples.layered.tsp;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the Traveling Salesman
 * Problem (TSP) instances.
 */
@Tag("non-regression")
public class TspNonRegressionTests {

    /**
     * Generates one dynamic non-regression test per Traveling Salesman Problem (TSP) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("TSP: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionTsp() {
        var supplier =
                new TSPTestDataSupplier(
                        Path.of("src", "test", "resources", "Non-Regression", "TSP"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
