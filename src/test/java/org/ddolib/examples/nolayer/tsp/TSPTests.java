package org.ddolib.examples.nolayer.tsp;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.nolayer.testbench.NoLayerTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the no-layer solvers on Traveling Salesman Problem (TSP) instances, using the {@link
 * NoLayerTestBench}.
 */
public class TSPTests {

    /**
     * Generates the dynamic tests for each Traveling Salesman Problem (TSP) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("TSP")
    @TestFactory
    public Stream<DynamicTest> testTSP() {
        var dataSupplier = new TSPTestDataSupplier(Path.of("src", "test", "resources", "TSP"));
        var bench = new NoLayerTestBench<>(dataSupplier);
        return bench.generateTests();
    }
}
