package org.ddolib.ddo.examples.mcp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.heuristics.VariableHeuristic;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;
import org.ddolib.ddo.implem.solver.SequentialSolver;

import java.io.IOException;
import java.util.Arrays;

public final class MCP {

    public static void main(String[] args) throws IOException {

        int n = 35;
        String filename = String.format("data/MCP/nodes_%d.txt", n);
        MCPIO.writeInstance(filename, n, 5);
        final MCPProblem problem = MCPIO.readInstance(filename);
        //System.out.println(problem.graph);


        final MCPRelax relax = new MCPRelax(problem);
        final MCPRanking ranking = new MCPRanking();

        final FixedWidth<MCPState> width = new FixedWidth<>(1000);
        final VariableHeuristic<MCPState> varh = new DefaultVariableHeuristic<>();
        final SimpleFrontier<MCPState> frontier = new SimpleFrontier<>(ranking);

        SequentialSolver<MCPState> solver = new SequentialSolver<>(problem, relax, varh, ranking, width, frontier);

        long start = System.currentTimeMillis();
        solver.maximize();
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] values = new int[problem.nbVars()];
                    for (Decision d : decisions) {
                        values[d.var()] = d.val();
                    }
                    return values;
                })
                .get();

        String[] solutionStr = Arrays.stream(solution).mapToObj(x -> x == 0 ? "S" : "T").toArray(String[]::new);
        System.out.printf("Instance: %s%n", filename);
        System.out.printf("Nodes: %d - Edges: %d%n", problem.graph.numNodes, problem.graph.numEdges);
        System.out.printf("Duration : %.3f seconds%n", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.printf("Solution : %s%n", Arrays.toString(solutionStr));
    }
}
