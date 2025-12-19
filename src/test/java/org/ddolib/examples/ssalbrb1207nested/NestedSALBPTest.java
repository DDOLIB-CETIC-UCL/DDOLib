package org.ddolib.examples.ssalbrb1207nested;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class NestedSALBPTest {
    @DisplayName("NestedSALBP")
    @TestFactory
    public Stream<DynamicTest> testNestedSALBP() {
        var dataSupplier =
                new NestedSALBPTestDataSupplier(Path.of("src", "test", "resources", "NestedSALBP"));
        var bench = new ProblemTestBench<>(dataSupplier);

        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}
