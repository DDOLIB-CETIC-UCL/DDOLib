package org.ddolib.examples.layered.talentscheduling;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the layered solvers on talent scheduling instances, using the {@link ProblemTestBench}.
 */
public class TalentSchedTest {

    /**
     * Generates the dynamic tests for each talent scheduling instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("Talent Scheduling")
    @TestFactory
    public Stream<DynamicTest> testTS() {
        var dataSupplier =
                new TalentSchedTestDataSupplier(
                        Path.of("src", "test", "resources", "TalentScheduling"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testLns = false;
        return bench.generateTests();
    }
}
