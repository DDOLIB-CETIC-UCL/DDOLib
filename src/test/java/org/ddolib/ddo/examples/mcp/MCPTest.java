package org.ddolib.ddo.examples.mcp;

import org.ddolib.ddo.core.CutSetType;
import org.ddolib.ddo.examples.mcp.*;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;
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
        final MCPRelax relax = new MCPRelax(problem);

        HashSet<Integer> vars = new HashSet<>();
        for (int i = 0; i < problem.nbVars(); i++) {
            vars.add(i);
        }

        int rub = relax.fastUpperBound(problem.initialState(), vars);

        assertTrue(rub >= problem.optimal.get(),
                String.format("Upper bound %d is not bigger than the expected optimal solution %d",
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
