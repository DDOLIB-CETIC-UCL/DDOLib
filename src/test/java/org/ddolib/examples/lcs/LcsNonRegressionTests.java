package org.ddolib.examples.lcs;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

@Tag("non-regression")
public class LcsNonRegressionTests {


    @DisplayName("LCS: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionLcs() {
        var supplier =
                new LCSTestDataSupplier(Path.of("src", "test", "resources", "Non-Regression",
                        "LCS"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
