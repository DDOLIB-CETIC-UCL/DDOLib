package org.ddolib.examples.layered.max2sat;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/** Tests of the layered solvers on Max2Sat instances, using the {@link ProblemTestBench}. */
public class Max2SatTest {

    /**
     * Generates the dynamic tests for each Max2Sat instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("Max2Sat")
    @TestFactory
    public Stream<DynamicTest> testMax2Sat() {
        var dataSupplier =
                new Max2SatTestDataSupplier(Path.of("src", "test", "resources", "Max2Sat"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}
