package org.ddolib.examples.misp;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Paths;
import java.util.stream.Stream;

public class MispTest {

    @DisplayName("MISP")
    @TestFactory
    public Stream<DynamicTest> testMISP() {
        var dataSupplier = new MispTestDataSupplier(Paths.get("src", "test", "resources", "MISP").toString());
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testDominance = true;
        bench.testCache = true;
        return bench.generateTests();
    }


}
