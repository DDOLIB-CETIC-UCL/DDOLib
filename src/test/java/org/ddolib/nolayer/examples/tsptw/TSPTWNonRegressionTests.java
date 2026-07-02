package org.ddolib.nolayer.examples.tsptw;

import org.ddolib.util.testbench.NoLayerNonRegressionTestBench;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class TSPTWNonRegressionTests {

    @TestFactory
    Stream<DynamicTest> testTSPTW() {
        Path testDataDir = Paths.get("src", "test", "resources", "TSPTW");
        TSPTWTestDataSupplier testDataSupplier = new TSPTWTestDataSupplier(testDataDir);
        NoLayerNonRegressionTestBench<TSPTWState, TSPTWProblem> testBench = new NoLayerNonRegressionTestBench<>(testDataSupplier);
        return testBench.generateTests();
    }
}
