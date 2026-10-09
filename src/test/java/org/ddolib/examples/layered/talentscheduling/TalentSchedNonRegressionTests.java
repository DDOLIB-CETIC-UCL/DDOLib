package org.ddolib.examples.layered.talentscheduling;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the talent scheduling
 * instances.
 */
@Tag("non-regression")
public class TalentSchedNonRegressionTests {
    /**
     * Generates one dynamic non-regression test per talent scheduling instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("Talent Sched: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionTalentSched() {
        var supplier =
                new TalentSchedTestDataSupplier(
                        Path.of("src", "test", "resources", "Non-Regression", "TalentScheduling"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
