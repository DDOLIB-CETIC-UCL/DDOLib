package org.ddolib.examples.mcp;

import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.nonregression.NonRegressionTestBench;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.verbosity.VerbosityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Tag("non-regression")
public class McpNonRegressionTest {
    @DisplayName("MCP: non regression")
    @TestFactory
    public Stream<DynamicTest> nonRegressionMcp() {
        var bench = new NonRegressionMcpBench();
        return bench.generateTests();
    }

    private static class NonRegressionMcpBench extends NonRegressionTestBench<MCPState, MCPProblem> {

        protected NonRegressionMcpBench() {
            super();
        }

        @Override
        protected List<MCPProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "Non-Regression", "MCP").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            return new MCPProblem(filePath.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected DdoModel<MCPState> model(MCPProblem problem) {
            return new DdoModel<>() {

                @Override
                public Problem<MCPState> problem() {
                    return problem;
                }

                @Override
                public MCPFastLowerBound lowerBound() {
                    return new MCPFastLowerBound(problem);
                }

                @Override
                public VerbosityLevel verbosityLevel() {
                    return VerbosityLevel.SILENT;
                }

                @Override
                public DebugLevel debugMode() {
                    return DebugLevel.ON;
                }

                @Override
                public Relaxation<MCPState> relaxation() {
                    return new MCPRelax(problem);
                }

                @Override
                public MCPRanking ranking() {
                    return new MCPRanking();
                }
            };
        }

    }
}