package org.ddolib.examples.hrc;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class HRCTest {

    @DisplayName("HRC")
    @TestFactory
    public Stream<DynamicTest> testHRC() {
        var dataSupplier = new HRCTestDataSupplier(Path.of("src", "test", "resources", "HRC"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = false;
        bench.testFLB = true;
        bench.testDominance = true;
        bench.testCache = false;
        return bench.generateTests();
    }
}

