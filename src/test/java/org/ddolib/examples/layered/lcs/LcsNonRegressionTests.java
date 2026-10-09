package org.ddolib.examples.layered.lcs;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the Longest Common
 * Subsequence (LCS) instances.
 */
@Tag("non-regression")
public class LcsNonRegressionTests {

    /**
     * Generates one dynamic non-regression test per Longest Common Subsequence (LCS) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("LCS: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionLcs() {
        var supplier =
                new LCSTestDataSupplier(
                        Path.of("src", "test", "resources", "Non-Regression", "LCS"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
