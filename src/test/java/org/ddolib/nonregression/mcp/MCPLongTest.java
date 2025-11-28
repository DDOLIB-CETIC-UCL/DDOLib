package org.ddolib.nonregression.mcp;

import org.ddolib.examples.mcp.*;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.util.debug.DebugLevel;
import org.ddolib.util.testbench.ProblemTestBench;
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
public class MCPLongTest {

    private static class MCPBench extends ProblemTestBench<MCPState, MCPProblem> {

        public MCPBench() {
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
                public Relaxation<MCPState> relaxation() {
                    return new MCPRelax(problem);
                }

                @Override
                public MCPRanking ranking() {
                    return new MCPRanking();
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
            };

        }
    }

    @DisplayName("MCP")
    @TestFactory
    public Stream<DynamicTest> testMCP() {
        var bench = new MCPBench();
        bench.testRelaxation = true;
        bench.testFLB = true;
        return bench.generateTests();
    }
}
