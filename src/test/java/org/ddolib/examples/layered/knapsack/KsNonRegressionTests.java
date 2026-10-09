package org.ddolib.examples.layered.knapsack;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/** Non-regression tests checking that all the layered solvers agree on the knapsack instances. */
@Tag("non-regression")
public class KsNonRegressionTests {

    /**
     * Generates one dynamic non-regression test per knapsack instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("KS: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionKs() {
        var supplier =
                new KSTestDataSupplier(
                        Path.of("src", "test", "resources", "Non-Regression", "Knapsack"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
