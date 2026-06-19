package org.ddolib.examples.tspnolayer;

import org.ddolib.astar.core.solver.NoLayerAStarSolver;
import org.ddolib.common.solver.Solution;
import org.ddolib.examples.tsp.TSPGenerator;
import org.ddolib.examples.tsp.TSPState;
import org.ddolib.modeling.nolayer.NoLayerModel;

import java.util.Arrays;
import java.util.Optional;

public class TSPNoLayerAStarMain {
    public static void main(String[] args) {
        // Generate a small random TSP instance
        TSPGenerator generator = new TSPGenerator(12, 42, 100); // Use a small size for fast execution
        double[][] distMatrix = generator.distanceMatrix;

        TSPNoLayerProblem problem = new TSPNoLayerProblem(distMatrix);
        NoLayerModel<TSPState> model = new TSPNoLayerModel(problem);

        NoLayerAStarSolver<TSPState> solver = new NoLayerAStarSolver<>(model);
        
        System.out.println("Starting A* Search on TSPNoLayer Problem...");
        Solution solution = solver.minimize(
                stats -> false,
                (sol, stats) -> {
                    System.out.println("Found a solution with value: " + stats.incumbent());
                    System.out.println("Path: " + Arrays.toString(sol));
                }
        );

        Optional<Double> bestVal = solver.bestValue();
    }
}
