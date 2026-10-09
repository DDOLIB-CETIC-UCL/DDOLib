package org.ddolib.examples.layered.mks;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the layered solvers on multi-dimensional knapsack (MKS) instances, using the {@link
 * ProblemTestBench}.
 */
public class MKSTest {
    /**
     * Generates the dynamic tests for each multi-dimensional knapsack (MKS) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("MKS")
    @TestFactory
    public Stream<DynamicTest> testMaxCover() {
        var dataSupplier = new MKSTestDataSupplier(Path.of("src", "test", "resources", "MKS"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = false;
        bench.testDominance = false;
        bench.testCache = false;
        bench.testLns = false;
        return bench.generateTests();
    }
}
