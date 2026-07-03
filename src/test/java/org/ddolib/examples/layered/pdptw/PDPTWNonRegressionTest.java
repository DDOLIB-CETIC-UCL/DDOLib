package org.ddolib.examples.layered.pdptw;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

@Tag("non-regression")
public class PDPTWNonRegressionTest {

    @DisplayName("PDPTW: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionPDPTW() {
        var supplier = new PDPTWTestDataSupplier(Path.of("src", "test", "resources", "Non-Regression", "PDPTW"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
