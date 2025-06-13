package org.ddolib.ddo.examples.tsp;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;

import java.io.IOException;
import java.util.Arrays;

import static org.ddolib.ddo.implem.solver.Solvers.parallelSolver;

public class TSPMain {

    public static void main(final String[] args) throws IOException {

        final TSPProblem problem = new TSPProblem(TSPInstance.randomMatrix(17));
        final TSPRelax relax = new TSPRelax(problem);
        final TSPRanking ranking = new TSPRanking();
        final FixedWidth<TSPState> width = new FixedWidth<>(500);
        final DefaultVariableHeuristic<TSPState> varh = new DefaultVariableHeuristic<>();

        final Frontier<TSPState> frontier = new SimpleFrontier<>(ranking,  CutSetType.LastExactLayer);
        final Solver solver = parallelSolver(
                Runtime.getRuntime().availableProcessors() / 2,
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        long start = System.currentTimeMillis();
        SearchStatistics stats = solver.maximize(1);
        double duration = (System.currentTimeMillis() - start) / 1000.0;

        int[] solution = solver.bestSolution()
                .map(decisions -> {
                    int[] route = new int[problem.nbVars() + 1];
                    route[0] = 0;
                    for (Decision d : decisions) {
                        route[d.var() + 1] = d.val();
                    }
                    return route;
                })
                .get();

        System.out.println(stats);
        System.out.printf("Duration : %.3f%n (s)", duration);
        System.out.printf("Objective: %d%n", solver.bestValue().get());
        System.out.println("eval from scratch: " + problem.eval(solution));
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }
}
