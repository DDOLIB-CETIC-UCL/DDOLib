package org.ddolib.examples.setcover;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class SetCoverTest {
    @DisplayName("SetCover")
    @TestFactory
    public Stream<DynamicTest> testSetCover() {
        var dataSupplier = new SetCoverTestDataSupplier(Path.of("src", "test", "resources", "SetCover"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = false;
        bench.testDominance = false;
        bench.testCache = false;
        return bench.generateTests();
    }
}
