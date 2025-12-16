package org.ddolib.examples.ssalbrb;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class SSALBRBTest {
    @DisplayName("SSALBRB")
    @TestFactory
    public Stream<DynamicTest> testSSALBRB() {
        var dataSupplier =
                new SSALBRBTestDataSupplier(Path.of("src", "test", "resources", "SSALBRB"));
        var bench = new ProblemTestBench<>(dataSupplier);

//        bench.testRelaxation = true;
        bench.testFLB = true;
        //bench.testDominance = true;
        return bench.generateTests();
    }
}
