package org.ddolib.examples.talentscheduling;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class TalentSchedTest {


    @DisplayName("Talent Scheduling")
    @TestFactory
    public Stream<DynamicTest> testTS() {
        var dataSupplier =
                new TalentSchedTestDataSupplier(Path.of("src", "test", "resources",
                        "TalentScheduling"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testLns = false;
        return bench.generateTests();
    }

}
