package org.ddolib.examples.nolayer.gruler;

import java.util.stream.Stream;
import org.ddolib.nolayer.testbench.NoLayerTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/** Tests of the no-layer solvers on Golomb ruler instances, using the {@link NoLayerTestBench}. */
public class GRTest {
    /**
     * Generates the dynamic tests for each Golomb ruler instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("Golomb Ruler")
    @TestFactory
    public Stream<DynamicTest> testGR() {
        var dataSupplier = new GRTestDataSupplier();
        var bench = new NoLayerTestBench<>(dataSupplier);
        return bench.generateTests();
    }
}
