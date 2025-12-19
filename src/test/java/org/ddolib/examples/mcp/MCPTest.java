package org.ddolib.examples.mcp;

import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class MCPTest {


    @DisplayName("MCP")
    @TestFactory
    public Stream<DynamicTest> testMCP() {
        var dataSupplier =
                new MCPTestDataSupplier(Path.of("src", "test", "resources", "MCP"));
        var bench = new ProblemTestBench<>(dataSupplier);
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}
