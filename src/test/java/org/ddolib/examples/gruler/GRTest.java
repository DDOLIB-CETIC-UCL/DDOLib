package org.ddolib.examples.gruler;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

public class GRTest {

    @DisplayName("Golomb ruler")
    @TestFactory
    public Stream<DynamicTest> testGR() {
        var bench = new ProblemTestBench<>(new GRTestDataSupplier());
        bench.testRelaxation = true;
        return bench.generateTests();
    }
}
