package org.ddolib.examples.smic;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class SMICTest {


    @DisplayName("SMIC")
    @TestFactory
    public Stream<DynamicTest> testSMIC() {
        var dataSupplier =
                new SMICTestDataSupplier(Path.of("src", "test", "resources", "SMIC"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
//        bench.testFLB = true;
//        bench.testDominance = true;
        return bench.generateTests();
    }
}
