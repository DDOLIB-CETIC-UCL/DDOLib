package org.ddolib.nolayer.examples.tsp;

import org.ddolib.util.testbench.NoLayerTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class TSPTests {

    @DisplayName("TSP")
    @TestFactory
    public Stream<DynamicTest> testTSP() {
        var dataSupplier =
                new TSPTestDataSupplier(Path.of("src", "test", "resources", "TSP"));
        var bench = new NoLayerTestBench<>(dataSupplier);
        return bench.generateTests();
    }
}
