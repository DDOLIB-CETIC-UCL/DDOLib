package org.ddolib.ddo.examples.pdp;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.solver.Solver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import static java.lang.Math.max;
import static org.ddolib.factory.Solvers.sequentialSolver;

public final class PDPMain {

    /**
     * Generates a PDP problem with a single vehicle:
     * a TSP problem such that
     * nodes are grouped by pair: (pickup node; delivery node)
     * in a pair, the pickup node must be reached before the delivery node
     * the problem can also have "unrelated nodes" that are not involved in such a pair
     *
     * @param n         the number of nodes of the PDP problem
     * @param unrelated the number of nodes that are not involved in a pickup-delivery pair.
     *                  there might be one more unrelated node than specified here
     * @return a PDP problem
     */
    public static PDPInstance genInstance(int n, int unrelated, Random random) {

        int[] x = new int[n];
        int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            x[i] = random.nextInt(100);
            y[i] = random.nextInt(100);
        }

        double[][] distance = new double[n][];
        for (int i = 0; i < n; i++) {
            distance[i] = new double[n];
            for (int j = 0; j < n; j++) {
                distance[i][j] = dist(x[i] - x[j], y[i] - y[j]);
            }
        }

        HashMap<Integer, Integer> pickupToAssociatedDelivery = new HashMap<>();

        int numberOfPairs = Math.floorDiv(n - max(1, unrelated), 2);
        int firstDelivery = numberOfPairs + 1;
        for (int p = 1; p < firstDelivery; p++) {
            int d = firstDelivery + p - 1;
            pickupToAssociatedDelivery.put(p, d);
        }

        return new PDPInstance(distance, pickupToAssociatedDelivery);
    }

    static int dist(int dx, int dy) {
        return (int) Math.sqrt(dx * dx + dy * dy);
    }

    public static void main(final String[] args) throws IOException {

        final PDPInstance instance = genInstance(15, 2, new Random(1));
        final PDPProblem problem = new PDPProblem(instance);

        System.out.println("problem:" + problem);
        System.out.println("initState:" + problem.initialState());

        Solver solver = solveDPD(problem);
        PDPSolution solution = extractSolution(solver, problem);

        System.out.printf("Objective: %f%n", solver.bestValue().get());
        System.out.println("Eval from scratch: " + instance.eval(solution.solution));
        System.out.printf("Solution : %s%n", solution);
        System.out.println("Problem:" + problem);

        System.out.println("end");
    }

    public static Solver solveDPD(PDPProblem problem) {

        final PDPRelax relax = new PDPRelax(problem);
        final PDPRanking ranking = new PDPRanking();
        final PDPFastUpperBound fub = new PDPFastUpperBound(problem);
        final FixedWidth<PDPState> width = new FixedWidth<>(3000);
        final DefaultVariableHeuristic<PDPState> varh = new DefaultVariableHeuristic<>();

        final Frontier<PDPState> frontier = new SimpleFrontier<>(ranking, CutSetType.LastExactLayer);
        final Solver solver = sequentialSolver(
                problem,
                relax,
                varh,
                ranking,
                width,
                frontier,
                fub);

        SearchStatistics statistics = solver.maximize(0, false);

        return solver;
    }

    public static PDPSolution extractSolution(Solver solver, PDPProblem problem) {
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

        double value = -solver.bestValue().get();

        return new PDPSolution(problem, solution, value);
    }
}
