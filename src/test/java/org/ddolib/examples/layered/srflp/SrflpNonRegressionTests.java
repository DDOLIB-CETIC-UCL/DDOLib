package org.ddolib.examples.layered.srflp;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the Single-Row Facility
 * Layout Problem (SRFLP) instances.
 */
@Tag("non-regression")
public class SrflpNonRegressionTests {

    /**
     * Generates one dynamic non-regression test per Single-Row Facility Layout Problem (SRFLP)
     * instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("SRFLP: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionSrflp() {
        var supplier =
                new SRFLPTestDataSupplier(
                        Path.of("src", "test", "resources", "Non-Regression", "SRFLP"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
