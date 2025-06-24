package org.ddolib.ddo.examples.tsp;

import org.ddolib.ddo.core.*;
import org.ddolib.ddo.implem.frontier.SimpleFrontier;
import org.ddolib.ddo.implem.heuristics.DefaultVariableHeuristic;
import org.ddolib.ddo.implem.heuristics.FixedWidth;

import java.io.IOException;
import java.util.Arrays;

import static org.ddolib.ddo.implem.solver.Solvers.parallelSolver;
import static org.ddolib.ddo.implem.solver.Solvers.sequentialSolver;

public class TSPMain {

    public static void main(final String[] args) throws IOException {

        TSPInstance instance = new TSPInstance("data/TSP/gr21.xml");
        Solver solver = solveTSP(instance);

        TSPProblem problem = new TSPProblem(instance.distanceMatrix);
        int[] solution = extractSpolution(problem, solver);
        System.out.printf("Objective: %.1f%n", solver.bestValue().get());
        System.out.println("eval from scratch: " + problem.eval(solution));
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
    }

    public static int[] extractSpolution(TSPProblem problem, Solver solver){
        return solver.bestSolution()
                .map(decisions -> {
                    int[] route = new int[problem.nbVars() + 1];
                    route[0] = 0;
                    for (Decision d : decisions) {
                        route[d.var() + 1] = d.val();
                    }
                    return route;
                })
                .get();
    }
    public static Solver solveTSP(TSPInstance instance){
        final TSPProblem problem = new TSPProblem(instance.distanceMatrix);
        final TSPRelax relax = new TSPRelax(problem);
        final TSPRanking ranking = new TSPRanking();
        final FixedWidth<TSPState> width = new FixedWidth<>(500);
        final DefaultVariableHeuristic<TSPState> varh = new DefaultVariableHeuristic<>();

        final Frontier<TSPState> frontier = new SimpleFrontier<>(ranking,  CutSetType.LastExactLayer);
        final Solver solver = sequentialSolver(
//                Runtime.getRuntime().availableProcessors() / 2,
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier);

        SearchStatistics stats = solver.maximize(2);
        System.out.println(stats);

        return solver;
    }
}
