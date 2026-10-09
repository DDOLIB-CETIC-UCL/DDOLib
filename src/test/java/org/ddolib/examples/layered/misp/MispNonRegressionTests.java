package org.ddolib.examples.layered.misp;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the Maximum Independent Set
 * Problem (MISP) instances.
 */
@Tag("non-regression")
public class MispNonRegressionTests {

    /**
     * Generates one dynamic non-regression test per Maximum Independent Set Problem (MISP)
     * instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("MISP: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionMisp() {
        var supplier =
                new MispTestDataSupplier(
                        Path.of("src", "test", "resources", "Non-Regression", "MISP"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
