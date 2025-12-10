package org.ddolib.examples.tsptw;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

@Tag("non-regression")
public class TsptwNonRegressionTests {

    @DisplayName("TSPTW: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionTsptw() {
        var supplier =
                new TSPTWTestDataSupplier(Path.of("src", "test", "resources", "Non-Regression",
                        "TSPTW"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
