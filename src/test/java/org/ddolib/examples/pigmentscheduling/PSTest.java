package org.ddolib.examples.pigmentscheduling;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

class PSTest {

    @DisplayName("PSP")
    @TestFactory
    public Stream<DynamicTest> testPSP() {
        var dataSupplier =
                new PSTestDataSupplier(Path.of("src", "test", "resources", "PSP", "2items"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}