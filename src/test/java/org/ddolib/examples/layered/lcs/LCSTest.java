package org.ddolib.examples.layered.lcs;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the layered solvers on Longest Common Subsequence (LCS) instances, using the {@link
 * ProblemTestBench}.
 */
public class LCSTest {
    /**
     * Generates the dynamic tests for each Longest Common Subsequence (LCS) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("LCS")
    @TestFactory
    public Stream<DynamicTest> testLCS() {
        var dataSupplier = new LCSTestDataSupplier(Path.of("src", "test", "resources", "LCS"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}
