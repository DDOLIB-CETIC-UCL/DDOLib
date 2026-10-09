package org.ddolib.examples.layered.alp;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the layered solvers on Aircraft Landing Problem (ALP) instances, using the {@link
 * ProblemTestBench}.
 */
public class ALPTest {

    /**
     * Generates the dynamic tests for each Aircraft Landing Problem (ALP) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("ALP")
    @TestFactory
    public Stream<DynamicTest> testALP() {
        var dataSupplier = new ALPTestDataSupplier(Path.of("src", "test", "resources", "ALP"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}
