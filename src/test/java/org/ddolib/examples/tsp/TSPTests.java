package org.ddolib.examples.tsp;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Paths;
import java.util.stream.Stream;

public class TSPTests {

    @DisplayName("TSP")
    @TestFactory
    public Stream<DynamicTest> testTSP() {
        var dataSupplier =
                new TSPTestDataSupplier(Paths.get("src", "test", "resources", "TSP").toString());
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}
