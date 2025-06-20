package org.ddolib.ddo.examples.mcp;

import org.ddolib.ddo.core.CutSetType;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.dominance.DefaultDominanceChecker;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.util.testbench.ProblemTestBench;
import org.ddolib.ddo.util.testbench.SolverConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class MCPTest {

    private static class MCPBench extends ProblemTestBench<MCPState, Integer, MCPProblem> {

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
        protected SolverConfig<MCPState, Integer> configSolver(MCPProblem problem) {
            MCPRelax relax = new MCPRelax(problem);
            MCPRanking ranking = new MCPRanking();
            FixedWidth<MCPState> width = new FixedWidth<>(1000);
            VariableHeuristic<MCPState> varh = new DefaultVariableHeuristic<>();
            SimpleFrontier<MCPState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
            DefaultDominanceChecker<MCPState> dominanceChecker = new DefaultDominanceChecker<>();

            return new SolverConfig<>(relax, varh, ranking, width, frontier, dominanceChecker);
        }
    }

    @DisplayName("MCP")
    @TestFactory
    public Stream<DynamicTest> testMCP() {
        var bench = new MCPBench();
        return bench.generateTests();
    }
}
