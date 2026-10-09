package org.ddolib.examples.layered.maximumcoverage;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the layered solvers on maximum coverage instances, using the {@link ProblemTestBench}.
 */
public class MaxCoverTest {

    /**
     * Generates the dynamic tests for each maximum coverage instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("MaxCover")
    @TestFactory
    public Stream<DynamicTest> testMaxCover() {
        var dataSupplier =
                new MaxCoverTestDataSupplier(Path.of("src", "test", "resources", "MaxCover"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = false;
        bench.testDominance = false;
        bench.testCache = true;
        bench.testLns = false;
        return bench.generateTests();
    }
}
