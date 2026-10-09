package org.ddolib.examples.layered.mcp;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests of the layered solvers on Maximum Cut Problem (MCP) instances, using the {@link
 * ProblemTestBench}.
 */
public class MCPTest {

    /**
     * Generates the dynamic tests for each Maximum Cut Problem (MCP) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("MCP")
    @TestFactory
    public Stream<DynamicTest> testMCP() {
        var dataSupplier = new MCPTestDataSupplier(Path.of("src", "test", "resources", "MCP"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        bench.testLns = false;
        return bench.generateTests();
    }
}
