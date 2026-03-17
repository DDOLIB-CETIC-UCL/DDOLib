package org.ddolib.examples.binPacking;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

@Tag("non-regression")
public class BPPNonRegressionTests {

    @DisplayName("BPP: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionBpp() {
        var supplier =
                new BPPTestDataSupplier(Path.of("src", "test", "resources", "Non-Regression",
                        "BPP"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
