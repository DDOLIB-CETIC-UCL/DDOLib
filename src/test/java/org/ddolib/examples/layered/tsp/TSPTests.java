package org.ddolib.examples.layered.tsp;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the layered solvers on Traveling Salesman Problem (TSP) instances, using the {@link
 * ProblemTestBench}.
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
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testLns = false;
        return bench.generateTests();
    }
}
