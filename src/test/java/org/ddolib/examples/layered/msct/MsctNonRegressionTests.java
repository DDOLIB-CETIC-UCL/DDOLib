package org.ddolib.examples.layered.msct;

import java.nio.file.Paths;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the Minimum Sum of Completion
 * Times (MSCT) instances.
 */
@Tag("non-regression")
public class MsctNonRegressionTests {

    /**
     * Generates one dynamic non-regression test per Minimum Sum of Completion Times (MSCT)
     * instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("MSCT: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionMsct() {
        var supplier =
                new MsctNonRegressionDataSupplier(
                        Paths.get("src", "test", "resources", "Non-Regression", "MSCT").toString());
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
