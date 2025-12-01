package org.ddolib.examples.pigmentscheduling;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Paths;
import java.util.stream.Stream;

class PSTest {

    @DisplayName("PSP")
    @TestFactory
    public Stream<DynamicTest> testPSP() {
        var dataSupplier =
                new PSTestDataSupplier(Paths.get("src", "test", "resources", "PSP", "2items").toString());
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}