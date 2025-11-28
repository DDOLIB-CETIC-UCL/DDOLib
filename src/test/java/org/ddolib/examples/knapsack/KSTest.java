package org.ddolib.examples.knapsack;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Paths;
import java.util.stream.Stream;

public class KSTest {
    @DisplayName("Knapsack")
    @TestFactory
    public Stream<DynamicTest> testKS() {
        var dataSupplier = new KSTestDataSupplier(Paths.get("src", "test", "resources", "Knapsack").toString());
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testDominance = true;
        bench.testCache = true;
        return bench.generateTests();
    }
}
