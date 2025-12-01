package org.ddolib.examples.srflp;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Paths;
import java.util.stream.Stream;

@Tag("non-regression")
public class SrflpNonRegressionTests {

    @DisplayName("SRFLP: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionSrflp() {
        var supplier =
                new SRFLPTestDataSupplier(Paths.get("src", "test", "resources", "Non-Regression",
                        "SRFLP").toString());
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
