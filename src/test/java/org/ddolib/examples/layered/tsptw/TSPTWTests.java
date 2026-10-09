package org.ddolib.examples.layered.tsptw;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the layered solvers on Traveling Salesman Problem with Time Windows (TSPTW) instances,
 * using the {@link ProblemTestBench}.
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
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testDominance = true;
        bench.testCache = true;
        bench.testLns = false;
        return bench.generateTests();
    }
}
