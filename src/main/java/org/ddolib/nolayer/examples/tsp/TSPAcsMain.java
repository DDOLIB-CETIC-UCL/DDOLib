package org.ddolib.nolayer.examples.tsp;

import org.ddolib.nolayer.common.solver.Solution;
import org.ddolib.nolayer.modeling.AcsModel;
import org.ddolib.nolayer.modeling.FastLowerBound;
import org.ddolib.nolayer.modeling.NoLayerDominanceChecker;
import org.ddolib.nolayer.modeling.Problem;

public class TSPAcsMain {
    public static void main(String[] args) {
        // Generate a small random TSP instance
        TSPGenerator generator = new TSPGenerator(12, 42, 100); // Use a small size for fast execution
        double[][] distMatrix = generator.distanceMatrix;

        TSPProblem problem = new TSPProblem(distMatrix);
        TSPModel baseModel = new TSPModel(problem);

        final AcsModel<TSPState> model = new AcsModel<>() {
            @Override
            public Problem<TSPState> problem() {
                return problem;
            }

            @Override
            public FastLowerBound<TSPState> lowerBound() {
                return baseModel.lowerBound();
            }

            @Override
            public NoLayerDominanceChecker<TSPState> dominance() {
                return baseModel.dominance();
            }

            @Override
            public int columnWidth() {
                return 10;
            }
        };

        System.out.println("Starting ACS Search on TSPNoLayer Problem...");
        Solution solution = org.ddolib.nolayer.modeling.Solvers.minimizeAcs(model,
                stats -> false,
                (sol, stats) -> {
                    System.out.println("Found a solution with value: " + stats.incumbent());
                    System.out.println("Path: " + sol);
                }
        );

        System.out.println("Optimal TSP value: " + solution.value());
    }
}
