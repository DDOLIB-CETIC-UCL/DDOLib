package org.ddolib.examples.mcp;

import org.ddolib.util.testbench.NonRegressionTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Paths;
import java.util.stream.Stream;

@Tag("non-regression")
public class McpNonRegressionTest {
    @DisplayName("MCP: non regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionMcp() {
        var supplier =
                new MCPTestDataSupplier(Paths.get("src", "test", "resources", "Non-Regression",
                        "MCP").toString());
        var bench = new NonRegressionTestBench<>(supplier);
        return bench.generateTests();
    }
}