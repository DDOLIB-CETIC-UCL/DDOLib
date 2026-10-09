package org.ddolib.examples.layered.misp;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the layered solvers on Maximum Independent Set Problem (MISP) instances, using the
 * {@link ProblemTestBench}.
 */
public class MispTest {

    /**
     * Generates the dynamic tests for each Maximum Independent Set Problem (MISP) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("MISP")
    @TestFactory
    public Stream<DynamicTest> testMISP() {
        var dataSupplier = new MispTestDataSupplier(Path.of("src", "test", "resources", "MISP"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testDominance = true;
        bench.testCache = true;
        return bench.generateTests();
    }
}
