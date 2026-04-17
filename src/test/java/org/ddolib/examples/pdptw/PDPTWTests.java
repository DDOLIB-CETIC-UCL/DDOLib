package org.ddolib.examples.pdptw;

import org.ddolib.examples.mks.MKSTestDataSupplier;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class PDPTWTests {
    @DisplayName("PDPTW")
    @TestFactory
    public Stream<DynamicTest> testPDPTW() {
        var dataSupplier =
                new PDPTWTestDataSupplier(Path.of("src", "test", "resources", "PDPTW"));

        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.minWidth = 45;
        bench.maxWidth = 50;
        return bench.generateTests();
    }
}
