package org.ddolib.examples.smic;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

@Tag("non-regression")
public class SmicNonRegressionTests {

    @DisplayName("SMIC: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionSmic() {
        var supplier = new SMICTestDataSupplier(Path.of("src", "test", "resources", "Non-Regression",
                "SMIC"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
