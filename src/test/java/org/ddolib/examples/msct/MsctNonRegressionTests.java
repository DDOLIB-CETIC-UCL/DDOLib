package org.ddolib.examples.msct;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Paths;
import java.util.stream.Stream;

@Tag("non-regression")
public class MsctNonRegressionTests {

    @DisplayName("MSCT: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionMsct() {
        var supplier =
                new MsctNonRegressionDataSupplier(Paths.get("src", "test", "resources", "Non-Regression",
                        "MSCT").toString());
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
