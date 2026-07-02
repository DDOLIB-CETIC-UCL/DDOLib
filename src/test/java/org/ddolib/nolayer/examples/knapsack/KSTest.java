package org.ddolib.nolayer.examples.knapsack;

import org.ddolib.util.testbench.NoLayerTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class KSTest {
    @DisplayName("Knapsack")
    @TestFactory
    public Stream<DynamicTest> testKS() {
        var dataSupplier = new KSTestDataSupplier(Path.of("src", "test", "resources", "Knapsack"));
        var bench = new NoLayerTestBench<>(dataSupplier);
        return bench.generateTests();
    }
}
