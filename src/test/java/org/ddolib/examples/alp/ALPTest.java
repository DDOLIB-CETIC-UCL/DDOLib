package org.ddolib.examples.alp;


import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Paths;
import java.util.stream.Stream;

public class ALPTest {

    @DisplayName("ALP")
    @TestFactory
    public Stream<DynamicTest> testALP() {
        var dataSupplier =
                new ALPTestDataSupplier(Paths.get("src", "test", "resources", "ALP").toString());
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}
