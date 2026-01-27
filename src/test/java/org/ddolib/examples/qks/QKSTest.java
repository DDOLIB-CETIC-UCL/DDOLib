package org.ddolib.examples.qks;

import org.ddolib.examples.mks.MKSTestDataSupplier;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class QKSTest {
    @DisplayName("QKS")
    @TestFactory
    public Stream<DynamicTest> testMaxCover() {
        var dataSupplier = new QKSTestDataSupplier(Path.of("src", "test", "resources", "QKS"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testDominance = true;
        bench.testCache = true;
        return bench.generateTests();
    }
}
