package org.ddolib.examples.layered.smic;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the layered solvers on Single Machine with Inventory Constraint (SMIC) instances, using
 * the {@link ProblemTestBench}.
 */
public class SMICTest {

    /**
     * Generates the dynamic tests for each Single Machine with Inventory Constraint (SMIC)
     * instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("SMIC")
    @TestFactory
    public Stream<DynamicTest> testSMIC() {
        var dataSupplier = new SMICTestDataSupplier(Path.of("src", "test", "resources", "SMIC"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testDominance = true;
        return bench.generateTests();
    }
}
