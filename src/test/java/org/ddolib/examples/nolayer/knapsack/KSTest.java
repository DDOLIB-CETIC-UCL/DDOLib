package org.ddolib.examples.nolayer.knapsack;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.nolayer.testbench.NoLayerTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/** Tests of the no-layer solvers on knapsack instances, using the {@link NoLayerTestBench}. */
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
        var bench = new NoLayerTestBench<>(dataSupplier);
        return bench.generateTests();
    }
}
