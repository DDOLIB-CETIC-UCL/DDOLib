package org.ddolib.examples.layered.knapsack;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/** Tests of the layered solvers on knapsack instances, using the {@link ProblemTestBench}. */
public class KSTest {
    /**
     * Generates the dynamic tests for each knapsack instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("Knapsack")
    @TestFactory
    public Stream<DynamicTest> testKS() {
        var dataSupplier = new KSTestDataSupplier(Path.of("src", "test", "resources", "Knapsack"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testDominance = true;
        bench.testCache = true;
        return bench.generateTests();
    }
}
