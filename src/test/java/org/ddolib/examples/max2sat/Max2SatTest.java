package org.ddolib.examples.max2sat;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Paths;
import java.util.stream.Stream;

public class Max2SatTest {

    @DisplayName("Max2Sat")
    @TestFactory
    public Stream<DynamicTest> testMax2Sat() {
        var dataSupplier =
                new Max2SatTestDataSupplier(Paths.get("src", "test", "resources", "Max2Sat").toString());
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }


}
