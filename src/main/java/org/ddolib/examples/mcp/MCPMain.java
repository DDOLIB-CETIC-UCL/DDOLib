package org.ddolib.examples.mcp;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.solver.SequentialSolver;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;

public final class MCPMain {

    public static void main(String[] args) throws IOException {

        final String filename = args.length == 0 ?
                Paths.get("data", "MCP", "mcp_5_2.txt").toString() : args[0];
        final int w = args.length == 2 ? Integer.parseInt(args[1]) : 2;

        SolverConfig<MCPState> config = new SolverConfig<>();
        final MCPProblem problem = MCPIO.readInstance(filename);
        config.problem = problem;
        config.relax = new MCPRelax(problem);
        config.ranking = new MCPRanking();
        config.flb = new MCPFastLowerBound(problem);

        config.width = new FixedWidth<>(2);
        config.varh = new DefaultVariableHeuristic<>();
        config.frontier = new SimpleFrontier<>(config.ranking, CutSetType.LastExactLayer);
        config.exportAsDot = true;

        final Solver solver = new SequentialSolver<>(config);

        long start = System.currentTimeMillis();
        config.verbosityLevel = 3;
        var stat = solver.minimize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.constructBestSolution(problem.nbVars());

        HashSet<Integer> s = new HashSet<>();
        HashSet<Integer> t = new HashSet<>();
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == 0) s.add(i);
            else t.add(i);
        }


        System.out.printf("Instance: %s%n", filename);
        System.out.printf("Nodes: %d - Edges: %d%n", problem.graph.numNodes, problem.graph.numEdges);
        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %f%n", solver.bestValue().get());
        System.out.printf("Solution : S = %s T = %s%n", s, t);
        System.out.println(stat);

    }
}
