package org.ddolib.examples.pdp;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class PDPTests {

    @DisplayName("PDP")
    @TestFactory
    public Stream<DynamicTest> testPDP() {
        var dataSupplier =
                new PDPTestDataSupplier(Path.of("src", "test", "resources", "PDP"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.minWidth = 45;
        bench.maxWidth = 50;
        return bench.generateTests();
    }
}
