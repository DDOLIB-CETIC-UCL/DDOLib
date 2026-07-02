package org.ddolib.nolayer.examples.tsp;

import org.ddolib.util.testbench.NoLayerNonRegressionTestBench;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class TSPNonRegressionTests {

    @TestFactory
    Stream<DynamicTest> testTSP() {
        Path testDataDir = Paths.get("src", "test", "resources", "TSP");
        TSPTestDataSupplier testDataSupplier = new TSPTestDataSupplier(testDataDir);
        NoLayerNonRegressionTestBench<TSPState, TSPProblem> testBench = new NoLayerNonRegressionTestBench<>(testDataSupplier);
        return testBench.generateTests();
    }
}
