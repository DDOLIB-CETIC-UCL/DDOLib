package org.ddolib.solving.astar.core.solver.nolayer;

import org.ddolib.common.solver.Solution;
import org.ddolib.examples.layered.tsp.TSPGenerator;
import org.ddolib.examples.nolayer.tsp.TSPState;
import org.ddolib.examples.nolayer.tsp.TSPModel;
import org.ddolib.examples.nolayer.tsp.TSPProblem;
import org.ddolib.modeling.nolayer.Model;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AStarSolverTest {

    @Test
    public void testTSPAStarSolver() {
        // Generate a random TSP instance
        TSPGenerator generator = new TSPGenerator(10, 42, 100);
        double[][] distMatrix = generator.distanceMatrix;

        // Solve with the layered API to get optimal
        org.ddolib.examples.layered.tsp.TSPProblem layeredProblem = new org.ddolib.examples.layered.tsp.TSPProblem(distMatrix);
        org.ddolib.modeling.layered.Model<org.ddolib.examples.layered.tsp.TSPState> layeredModel = new org.ddolib.modeling.layered.Model<>() {
            @Override
            public org.ddolib.modeling.layered.Problem<org.ddolib.examples.layered.tsp.TSPState> problem() {
                return layeredProblem;
            }
            @Override
            public org.ddolib.modeling.layered.FastLowerBound<org.ddolib.examples.layered.tsp.TSPState> lowerBound() {
                return new org.ddolib.examples.layered.tsp.TSPFastLowerBound(layeredProblem);
            }
        };
        org.ddolib.solving.astar.core.solver.layered.AStarSolver<org.ddolib.examples.layered.tsp.TSPState> layeredSolver = new org.ddolib.solving.astar.core.solver.layered.AStarSolver<>(layeredModel);
        layeredSolver.minimize(stats -> false, (sol, stats) -> {});
        double expectedOptimal = layeredSolver.bestValue().orElseThrow();

        // Solve with the new NoLayer API
        TSPProblem noLayerProblem = new TSPProblem(distMatrix);
        Model<TSPState> noLayerModel = new TSPModel(noLayerProblem);

        AStarSolver<TSPState> noLayerSolver = new AStarSolver<>(noLayerModel);
        
        Solution solution = noLayerSolver.minimize(
                stats -> false,
                (sol, stats) -> {}
        );

        Optional<Double> noLayerOptimal = noLayerSolver.bestValue();
        
        assertTrue(noLayerOptimal.isPresent(), "Solver should find a solution");
        assertEquals(expectedOptimal, noLayerOptimal.get(), 1e-6, "NoLayer solver should find the same optimal value");
    }
}
