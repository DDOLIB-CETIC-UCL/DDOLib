package org.ddolib.ddo.examples.mcp;

import org.ddolib.ddo.core.CutSetType;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Solver;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;

public final class MCPMain {

    public static void main(String[] args) throws IOException {

        final String filename = args.length == 0 ? Paths.get("data", "MCP", "mcp_4.txt").toString() : args[0];
        final int w = args.length == 2 ? Integer.parseInt(args[1]) : 500;

        final MCPProblem problem = MCPIO.readInstance(filename);

        final MCPRelax relax = new MCPRelax(problem);
        final MCPRanking ranking = new MCPRanking();

        final FixedWidth<MCPState> width = new FixedWidth<>(w);
        final VariableHeuristic<MCPState> varh = new DefaultVariableHeuristic<>();
        final SimpleFrontier<MCPState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);

        final Solver solver = sequentialSolver(problem, relax, varh, ranking, width, frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        Optional<Set<Decision>> bestSol = solver.bestSolution();

        int[] solution = bestSol.map(decisions -> {
            int[] values = new int[problem.nbVars()];
            for (Decision d : decisions) {
                values[d.var()] = d.val();
            }
            return values;
        }).get();

        HashSet<Integer> s = new HashSet<>();
        HashSet<Integer> t = new HashSet<>();
        for (int i = 0; i < solution.length; i++) {
            if (solution[i] == 0) s.add(i);
            else t.add(i);
        }


        System.out.printf("Instance: %s%n", filename);
        System.out.printf("Nodes: %d - Edges: %d%n", problem.graph.numNodes, problem.graph.numEdges);
        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.printf("Solution : S = %s T = %s%n", s, t);


    }
}
