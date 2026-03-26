package org.ddolib.examples.hrcp;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class HRCPTest {

    @DisplayName("HRCP")
    @TestFactory
    public Stream<DynamicTest> testHRCP() {
        var dataSupplier = new HRCPTestDataSupplier(Path.of("src", "test", "resources", "HRCP"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = false;
        bench.testFLB = true;
        bench.testDominance = true;
        bench.testCache = false;
        return bench.generateTests();
    }
}

