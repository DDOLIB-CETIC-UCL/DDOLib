package org.ddolib.examples.bddmaximumcoverage;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class MaxCoverTest {

    @DisplayName("MaxCover")
    @TestFactory
    public Stream<DynamicTest> testMaxCover() {
        var dataSupplier = new MaxCoverTestDataSupplier(Path.of("src", "test", "resources", "MaxCover"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = false;
        bench.testDominance = false;
        bench.testCache = true;
        return bench.generateTests();
    }
}
