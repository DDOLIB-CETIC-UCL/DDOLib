package org.ddolib.nolayer.examples.misp;

import org.ddolib.util.testbench.NoLayerTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class MispTest {

    @DisplayName("MISP")
    @TestFactory
    public Stream<DynamicTest> testMISP() {
        var dataSupplier =
                new MispTestDataSupplier(Path.of("src", "test", "resources", "MISP"));
        var bench = new NoLayerTestBench<>(dataSupplier);
        return bench.generateTests();
    }
}
