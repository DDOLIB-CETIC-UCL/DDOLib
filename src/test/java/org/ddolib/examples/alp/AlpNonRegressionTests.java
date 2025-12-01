package org.ddolib.examples.alp;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Paths;
import java.util.stream.Stream;

@Tag("non-regression")
public class AlpNonRegressionTests {

    @DisplayName("ALP: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionAlp() {
        var supplier =
                new ALPTestDataSupplier(Paths.get("src", "test", "resources", "Non-Regression",
                        "ALP").toString());
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
