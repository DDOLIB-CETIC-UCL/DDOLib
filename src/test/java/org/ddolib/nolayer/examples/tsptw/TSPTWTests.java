package org.ddolib.nolayer.examples.tsptw;

import org.ddolib.util.testbench.NoLayerTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class TSPTWTests {

    @DisplayName("TSPTW")
    @TestFactory
    public Stream<DynamicTest> testTSPTW() {
        var dataSupplier =
                new TSPTWTestDataSupplier(Path.of("src", "test", "resources", "TSPTW"));
        var bench = new NoLayerTestBench<>(dataSupplier);
        return bench.generateTests();
    }
}
