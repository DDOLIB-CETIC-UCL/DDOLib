package org.ddolib.examples.layered.srflp;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the layered solvers on Single-Row Facility Layout Problem (SRFLP) instances, using the
 * {@link ProblemTestBench}.
 */
public class SRFLPTest {

    /**
     * Generates the dynamic tests for each Single-Row Facility Layout Problem (SRFLP) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("SRFLP")
    @TestFactory
    public Stream<DynamicTest> testSRFLP() {
        var dataSupplier = new SRFLPTestDataSupplier(Path.of("src", "test", "resources", "SRFLP"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}
