package org.ddolib.examples.nolayer.tsp;

import org.ddolib.common.dominance.NoLayerDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.modeling.Solvers;
import org.ddolib.modeling.nolayer.NoLayerAcsModel;
import org.ddolib.modeling.nolayer.NoLayerFastLowerBound;
import org.ddolib.modeling.nolayer.NoLayerProblem;

import java.util.Arrays;
import java.util.Optional;

public class TSPAcsMain {
    public static void main(String[] args) {
        // Generate a small random TSP instance
        TSPGenerator generator = new TSPGenerator(12, 42, 100); // Use a small size for fast execution
        double[][] distMatrix = generator.distanceMatrix;

        TSPProblem problem = new TSPProblem(distMatrix);
        TSPModel baseModel = new TSPModel(problem);

        final NoLayerAcsModel<TSPState> model = new NoLayerAcsModel<>() {
            @Override
            public NoLayerProblem<TSPState> problem() {
                return problem;
            }

            @Override
            public NoLayerFastLowerBound<TSPState> lowerBound() {
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
        Solution solution = Solvers.minimizeNoLayerAcs(model,
                stats -> false,
                (sol, stats) -> {
                    System.out.println("Found a solution with value: " + stats.incumbent());
                    System.out.println("Path: " + Arrays.toString(sol));
                }
        );

        System.out.println("Optimal TSP value: " + solution.value());
    }
}
