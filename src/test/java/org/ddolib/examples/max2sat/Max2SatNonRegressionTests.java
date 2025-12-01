package org.ddolib.examples.max2sat;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Paths;
import java.util.stream.Stream;

@Tag("non-regression")
public class Max2SatNonRegressionTests {

    @DisplayName("Max2Sat: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionMax2Sat() {
        var supplier =
                new Max2SatTestDataSupplier(Paths.get("src", "test", "resources", "Non-Regression",
                        "Max2Sat").toString());
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
