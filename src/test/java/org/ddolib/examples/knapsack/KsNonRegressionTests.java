package org.ddolib.examples.knapsack;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

@Tag("non-regression")
public class KsNonRegressionTests {

    @DisplayName("KS: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionKs() {
        var supplier =
                new KSTestDataSupplier(Path.of("src", "test", "resources", "Non-Regression",
                        "Knapsack"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
