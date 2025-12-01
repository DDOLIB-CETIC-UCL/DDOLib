package org.ddolib.examples.talentscheduling;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Paths;
import java.util.stream.Stream;

@Tag("non-regression")
public class TalentSchedNonRegressionTests {
    @DisplayName("Talent Sched: non-regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionMisp() {
        var supplier =
                new TalentSchedTestDataSupplier(Paths.get("src", "test", "resources",
                        "Non-Regression", "TalentScheduling").toString());
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
