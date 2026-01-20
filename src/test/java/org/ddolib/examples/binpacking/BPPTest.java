package org.ddolib.examples.binpacking;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class BPPTest {

    @DisplayName("BPP")
    @TestFactory
    public Stream<DynamicTest> testBPP() {
        var dataSupplier =
                new BPPTestDataSupplier(Path.of("src", "test", "resources", "BinPacking"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testDominance = true;
        return bench.generateTests();
    }
}
