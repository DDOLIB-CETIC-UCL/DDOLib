package org.ddolib.examples.mcp;

import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.util.testbench.ProblemTestBench;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.lang.model.type.NullType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class MCPTest {

    private static class MCPBench extends ProblemTestBench<MCPState, NullType, MCPProblem> {

        public MCPBench() {
            super();
        }

        @Override
        protected List<MCPProblem> generateProblems() {
            String dir = Paths.get("src", "test", "resources", "MCP").toString();

            File[] files = new File(dir).listFiles();
            assert files != null;
            Stream<File> stream = Stream.of(files);

            return stream.filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .map(fileName -> Paths.get(dir, fileName))
                    .map(filePath -> {
                        try {
                            MCPProblem problem = MCPIO.readInstance(filePath.toString());
                            problem.setName(filePath.getFileName().toString());
                            return problem;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }

        @Override
        protected SolverConfig<MCPState, NullType> configSolver(MCPProblem problem) {
            SolverConfig<MCPState, NullType> config = new SolverConfig<>();
            config.problem = problem;
            config.relax = new MCPRelax(problem);
            config.ranking = new MCPRanking();
            config.flb = new MCPFastLowerBound(problem);

            config.width = new FixedWidth<>(10);
            config.varh = new DefaultVariableHeuristic<>();
            config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);


            return config;
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
