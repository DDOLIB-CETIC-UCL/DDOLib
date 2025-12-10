package org.ddolib.examples.tsp;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

@Tag("non-regression")
public class TspNonRegressionTests {

    @DisplayName("TSP: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionTsp() {
        var supplier =
                new TSPTestDataSupplier(Path.of("src", "test", "resources", "Non-Regression",
                        "TSP"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
