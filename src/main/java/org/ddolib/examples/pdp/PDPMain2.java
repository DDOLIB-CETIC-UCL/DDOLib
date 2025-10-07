package org.ddolib.examples.pdp;

import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.cache.SimpleCache;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.variable.DefaultVariableHeuristic;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.ddo.core.solver.SequentialSolver;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solve;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import static java.lang.Math.max;

public final class PDPMain2 {

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
    public static PDPInstance genInstance(int n, int unrelated, int maxCapa, Random random) {

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

        return new PDPInstance(distance, pickupToAssociatedDelivery, maxCapa);
    }

    static int dist(int dx, int dy) {
        return (int) Math.sqrt(dx * dx + dy * dy);
    }

    public static void main(final String[] args) throws IOException {

        final PDPInstance instance = genInstance(18, 2, 3, new Random(1));
        DdoModel<PDPState> model = new DdoModel<>(){
            private PDPProblem problem;
            @Override
            public Problem<PDPState> problem() {
                problem = new PDPProblem(instance);
                return problem;
            }
            @Override
            public PDPRelax relaxation() {
                return new PDPRelax(problem);
            }
            @Override
            public PDPRanking ranking() {
                return new PDPRanking();
            }

            @Override
            public PDPFastLowerBound lowerBound() {
                return new PDPFastLowerBound(problem);
            }
            @Override
            public boolean useCache() {
                return true;
            }
            @Override
            public WidthHeuristic<PDPState> widthHeuristic() {
                return new FixedWidth<>(1000);
            }
        };

        Solve<PDPState> solve = new Solve<>();

        SearchStatistics stats =  solve.minimizeDdo(model);

        solve.onSolution(stats);
    }
}
