package org.ddolib.examples.msct;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

class MSCTTest {

    @DisplayName("MSCT")
    @TestFactory
    public Stream<DynamicTest> testMSCT() {
        var bench = new ProblemTestBench<>(new MSCTTestDataSupplier());
        bench.testRelaxation = true;
        bench.testDominance = true;
        return bench.generateTests();
    }
}