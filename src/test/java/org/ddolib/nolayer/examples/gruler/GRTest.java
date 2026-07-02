package org.ddolib.nolayer.examples.gruler;

import org.ddolib.util.testbench.NoLayerTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

public class GRTest {
    @DisplayName("Golomb Ruler")
    @TestFactory
    public Stream<DynamicTest> testGR() {
        var dataSupplier = new GRTestDataSupplier();
        var bench = new NoLayerTestBench<>(dataSupplier);
        return bench.generateTests();
    }
}
