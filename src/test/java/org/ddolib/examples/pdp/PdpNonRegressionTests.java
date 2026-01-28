package org.ddolib.examples.pdp;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

@Tag("non-regression")
public class PdpNonRegressionTests {

    @DisplayName("PDP: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionPdp() {
        var supplier =
                new PDPTestDataSupplier(Path.of("src", "test", "resources", "Non-Regression",
                        "PDP"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
