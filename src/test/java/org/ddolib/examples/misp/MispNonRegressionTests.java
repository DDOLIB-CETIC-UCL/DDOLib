package org.ddolib.examples.misp;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

@Tag("non-regression")
public class MispNonRegressionTests {

    @DisplayName("MISP: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionMisp() {
        var supplier =
                new MispTestDataSupplier(Path.of("src", "test", "resources", "Non-Regression",
                        "MISP"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
