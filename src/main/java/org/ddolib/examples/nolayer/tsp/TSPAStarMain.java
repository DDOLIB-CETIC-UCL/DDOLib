package org.ddolib.examples.nolayer.tsp;

import org.ddolib.solving.astar.core.solver.nolayer.AStarSolver;
import org.ddolib.common.solver.layered.Solution;
import org.ddolib.modeling.nolayer.Model;

import java.util.Arrays;
import java.util.Optional;

public class TSPAStarMain {
    public static void main(String[] args) {
        // Generate a small random TSP instance
        TSPGenerator generator = new TSPGenerator(12, 42, 100); // Use a small size for fast execution
        double[][] distMatrix = generator.distanceMatrix;

        TSPProblem problem = new TSPProblem(distMatrix);
        Model<TSPState> model = new TSPModel(problem);

        AStarSolver<TSPState> solver = new AStarSolver<>(model);

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
