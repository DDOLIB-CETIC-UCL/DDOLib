package org.ddolib.examples.layered.pigmentscheduling;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class PSTest {

    /**
     * Generates the dynamic tests for each Pigment Sequencing Problem (PSP) instance.
     *
     * @return the stream of generated tests
     */
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
