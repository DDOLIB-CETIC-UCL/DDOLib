package org.ddolib.examples.nolayer.gruler;

import org.ddolib.util.testbench.NoLayerNonRegressionTestBench;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

public class GRNonRegressionTests {

    @TestFactory
    Stream<DynamicTest> testGRuler() {
        GRTestDataSupplier testDataSupplier = new GRTestDataSupplier();
        NoLayerNonRegressionTestBench<GRState, GRProblem> testBench = new NoLayerNonRegressionTestBench<>(testDataSupplier);
        return testBench.generateTests();
    }
}
