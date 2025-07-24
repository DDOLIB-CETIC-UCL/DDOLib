package org.ddolib.examples.ddo.mcp;

import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.variable.VariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.ddolib.factory.Solvers.sequentialSolver;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MCPTest {

    static Stream<MCPProblem> dataProvider() {
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
                });
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testMCP(MCPProblem problem) {

        final MCPRelax relax = new MCPRelax(problem);
        final MCPRanking ranking = new MCPRanking();

        final FixedWidth<MCPState> width = new FixedWidth<>(1000);
        final VariableHeuristic<MCPState> varh = new DefaultVariableHeuristic<>();
        final SimpleFrontier<MCPState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        final Solver solver = sequentialSolver(problem, relax, varh, ranking, width, frontier);

        solver.maximize();
        assertEquals(problem.optimal.get(), solver.bestValue().get());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testFastUpperBound(MCPProblem problem) {
        final MCPFastUpperBound fub = new MCPFastUpperBound(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        double rub = fub.fastUpperBound(problem.initialState(), vars);

        assertTrue(rub >= problem.optimal.get(),
                String.format("Upper bound %.2f is not bigger than the expected optimal solution %.2f",
                        rub,
                        problem.optimal.get()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testMCPWithRelax(MCPProblem problem) {

        final MCPRelax relax = new MCPRelax(problem);
        final MCPRanking ranking = new MCPRanking();

        final FixedWidth<MCPState> width = new FixedWidth<>(2);
        final VariableHeuristic<MCPState> varh = new DefaultVariableHeuristic<>();
        final SimpleFrontier<MCPState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        final Solver solver = sequentialSolver(problem, relax, varh, ranking, width, frontier);

        solver.maximize();
        assertEquals(problem.optimal.get(), solver.bestValue().get());
    }


}
