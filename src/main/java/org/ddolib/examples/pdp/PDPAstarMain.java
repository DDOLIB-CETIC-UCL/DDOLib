package org.ddolib.examples.pdp;

import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import static java.lang.Math.max;

public final class PDPAstarMain {


    public static void main(final String[] args) throws IOException {

        final PDPInstance instance = genInstance(18, 2, 3, new Random(1));
        Model<PDPState> model = new Model<>() {
            private PDPProblem problem;

            @Override
            public Problem<PDPState> problem() {
                problem = new PDPProblem(instance);
                return problem;
            }

            @Override
            public PDPFastLowerBound lowerBound() {
                return new PDPFastLowerBound(problem);
            }
        };

        Solver<PDPState> solver = new Solver<>();

        SearchStatistics stats = solver.minimize(model);

        solver.onSolution(stats);
    }

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
}
