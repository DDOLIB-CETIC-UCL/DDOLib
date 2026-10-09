package org.ddolib.examples.nolayer.tsptw;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.nolayer.testbench.NoLayerTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the no-layer solvers on Traveling Salesman Problem with Time Windows (TSPTW) instances,
 * using the {@link NoLayerTestBench}.
 */
public class TSPTWTests {

    /**
     * Generates the dynamic tests for each Traveling Salesman Problem with Time Windows (TSPTW)
     * instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("TSPTW")
    @TestFactory
    public Stream<DynamicTest> testTSPTW() {
        var dataSupplier = new TSPTWTestDataSupplier(Path.of("src", "test", "resources", "TSPTW"));
        var bench = new NoLayerTestBench<>(dataSupplier);
        return bench.generateTests();
    }
}
