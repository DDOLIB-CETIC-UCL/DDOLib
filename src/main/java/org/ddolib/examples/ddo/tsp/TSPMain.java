package org.ddolib.examples.ddo.tsp;

import org.ddolib.common.solver.Solver;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;

import java.io.IOException;
import java.util.Arrays;

import static org.ddolib.factory.Solvers.sequentialSolver;

public class TSPMain {

    public static void main(final String[] args) throws IOException {

        //TSPInstance instance = new TSPInstance("data/TSP/gr21.xml");
        TSPInstance instance = new TSPInstance("data/TSP/instance_8_0.xml");

        Solver solver = solveTSP(instance);

        TSPProblem problem = new TSPProblem(instance.distanceMatrix);
        int[] solution = extractSolution(problem, solver);
        System.out.printf("Objective: %.1f%n", solver.bestValue().get());
        System.out.println("eval from scratch: " + problem.eval(solution));
        System.out.printf("Solution : %s%n", Arrays.toString(solution));
        if (instance.objective >= 0) {
            System.out.println("real best: " + instance.objective);
        }
    }

    public static int[] extractSolution(TSPProblem problem, Solver solver) {
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

    public static Solver solveTSP(TSPInstance instance) {
        final TSPProblem problem = new TSPProblem(instance.distanceMatrix);
        final TSPRelax relax = new TSPRelax(problem);
        final TSPRanking ranking = new TSPRanking();
        final TSPFastUpperBound fub = new TSPFastUpperBound(problem);
        final FixedWidth<TSPState> width = new FixedWidth<>(500);
        final DefaultVariableHeuristic<TSPState> varh = new DefaultVariableHeuristic<>();

        final Frontier<TSPState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = sequentialSolver(
//                Runtime.getRuntime().availableProcessors() / 2,
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                fub);

        SearchStatistics stats = solver.maximize(2, false);
        System.out.println(stats);

        return solver;
    }
}
