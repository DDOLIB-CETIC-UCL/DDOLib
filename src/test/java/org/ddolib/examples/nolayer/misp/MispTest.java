package org.ddolib.examples.nolayer.misp;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.nolayer.testbench.NoLayerTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the no-layer solvers on Maximum Independent Set Problem (MISP) instances, using the
 * {@link NoLayerTestBench}.
 */
public class MispTest {

    /**
     * Generates the dynamic tests for each Maximum Independent Set Problem (MISP) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("MISP")
    @TestFactory
    public Stream<DynamicTest> testMISP() {
        var dataSupplier = new MispTestDataSupplier(Path.of("src", "test", "resources", "MISP"));
        var bench = new NoLayerTestBench<>(dataSupplier);
        return bench.generateTests();
    }
}
