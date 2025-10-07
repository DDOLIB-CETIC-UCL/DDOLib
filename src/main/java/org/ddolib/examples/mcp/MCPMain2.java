package org.ddolib.examples.mcp;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Relaxation;
import org.ddolib.modeling.Solve;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;

public final class MCPMain2 {

    public static void main(String[] args) throws IOException {

        final String filename = Paths.get("data", "MCP", "mcp_5_2.txt").toString();
        DdoModel<MCPState> model = new DdoModel<MCPState>() {
            private MCPProblem problem;
            @Override
            public Problem<MCPState> problem() {
                try {
                    problem = MCPIO.readInstance(filename);
                    return problem;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
        };

        Solve<MCPState> solve = new Solve<>();

        SearchStatistics stats = solve.minimizeDdo(model);

        solve.onSolution(stats);

    }
}
