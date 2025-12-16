package org.ddolib.examples.srflp;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class SRFLPTest {

    @DisplayName("SRFLP")
    @TestFactory
    public Stream<DynamicTest> testSRFLP() {
        var dataSupplier =
                new SRFLPTestDataSupplier(Path.of("src", "test", "resources", "SRFLP"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}
