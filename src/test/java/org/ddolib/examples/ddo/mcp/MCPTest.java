package org.ddolib.examples.ddo.mcp;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.lang.model.type.NullType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.Stream;

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

        SolverConfig<MCPState, NullType> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new MCPRelax(problem);
        config.ranking = new MCPRanking();

        config.width = new FixedWidth<>(1000);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

        final Solver solver = new SequentialSolver<>(config);

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

        SolverConfig<MCPState, NullType> config = new SolverConfig<>();
        config.problem = problem;
        config.relax = new MCPRelax(problem);
        config.ranking = new MCPRanking();

        config.width = new FixedWidth<>(2);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);

        final Solver solver = new SequentialSolver<>(config);

        solver.maximize();
        assertEquals(problem.optimal.get(), solver.bestValue().get());
    }


}
