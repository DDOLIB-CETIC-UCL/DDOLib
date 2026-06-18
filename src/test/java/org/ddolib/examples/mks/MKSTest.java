package org.ddolib.examples.mks;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class MKSTest {
    @DisplayName("MKS")
    @TestFactory
    public Stream<DynamicTest> testMaxCover() {
        var dataSupplier = new MKSTestDataSupplier(Path.of("src", "test", "resources", "MKS"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = false;
        bench.testDominance = false;
        bench.testCache = false;
        bench.testLns = false;
        return bench.generateTests();
    }

}
