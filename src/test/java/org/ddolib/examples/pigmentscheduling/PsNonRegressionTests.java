package org.ddolib.examples.pigmentscheduling;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Paths;
import java.util.stream.Stream;

@Tag("non-regression")
public class PsNonRegressionTests {
    @DisplayName("PS: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionPs() {
        var supplier =
                new PSTestDataSupplier(Paths.get("src", "test", "resources", "Non-Regression",
                        "PSP").toString());
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
