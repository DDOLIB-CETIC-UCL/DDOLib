package org.ddolib.examples.lcs;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Paths;
import java.util.stream.Stream;

public class LCSTest {
    @DisplayName("LCS")
    @TestFactory
    public Stream<DynamicTest> testLCS() {
        var dataSupplier =
                new LCSTestDataSupplier(Paths.get("src", "test", "resources", "LCS").toString());
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}
