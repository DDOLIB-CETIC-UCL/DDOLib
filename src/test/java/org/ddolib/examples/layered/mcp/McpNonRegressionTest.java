package org.ddolib.examples.layered.mcp;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.ddolib.layered.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

/**
 * Non-regression tests checking that all the layered solvers agree on the Maximum Cut Problem (MCP)
 * instances.
 */
@Tag("non-regression")
public class McpNonRegressionTest {
    /**
     * Generates one dynamic non-regression test per Maximum Cut Problem (MCP) instance.
     *
     * @return the stream of generated tests
     */
    @DisplayName("MCP: non regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionMcp() {
        var supplier =
                new MCPTestDataSupplier(
                        Path.of("src", "test", "resources", "Non-Regression", "MCP"));
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}
