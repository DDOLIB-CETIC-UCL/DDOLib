package org.ddolib.examples.layered.msct;

import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class MSCTTest {

    /**
     * Generates the dynamic tests for each Minimum Sum of Completion Times (MSCT) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("MSCT")
    @TestFactory
    public Stream<DynamicTest> testMSCT() {
        var bench = new ProblemTestBench<>(new MSCTTestDataSupplier());
        bench.testRelaxation = true;
        bench.testDominance = true;
        return bench.generateTests();
    }
}
