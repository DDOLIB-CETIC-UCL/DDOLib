package org.ddolib.nolayer.solving.astar.core.solver;

import org.ddolib.nolayer.common.solver.Solution;
import org.ddolib.layered.examples.tsp.TSPGenerator;
import org.ddolib.nolayer.examples.tsp.TSPState;
import org.ddolib.nolayer.examples.tsp.TSPModel;
import org.ddolib.nolayer.examples.tsp.TSPProblem;
import org.ddolib.nolayer.modeling.Model;
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
        org.ddolib.layered.examples.tsp.TSPProblem layeredProblem = new org.ddolib.layered.examples.tsp.TSPProblem(distMatrix);
        org.ddolib.layered.modeling.Model<org.ddolib.layered.examples.tsp.TSPState> layeredModel = new org.ddolib.layered.modeling.Model<>() {
            @Override
            public org.ddolib.layered.modeling.Problem<org.ddolib.layered.examples.tsp.TSPState> problem() {
                return layeredProblem;
            }

            @Override
            public org.ddolib.layered.modeling.FastLowerBound<org.ddolib.layered.examples.tsp.TSPState> lowerBound() {
                return new org.ddolib.layered.examples.tsp.TSPFastLowerBound(layeredProblem);
            }
        };
        org.ddolib.layered.solving.astar.core.solver.AStarSolver<org.ddolib.layered.examples.tsp.TSPState> layeredSolver = new org.ddolib.layered.solving.astar.core.solver.AStarSolver<>(layeredModel);
        layeredSolver.minimize(stats -> false, (sol, stats) -> {
        });
        double expectedOptimal = layeredSolver.bestValue().orElseThrow();

        // Solve with the new NoLayer API
        TSPProblem noLayerProblem = new TSPProblem(distMatrix);
        Model<TSPState> noLayerModel = new TSPModel(noLayerProblem);

        AStarSolver<TSPState> noLayerSolver = new AStarSolver<>(noLayerModel);

        Solution solution = noLayerSolver.minimize(
                stats -> false,
                (sol, stats) -> {
                }
        );

        Optional<Double> noLayerOptimal = noLayerSolver.bestValue();

        assertTrue(noLayerOptimal.isPresent(), "Solver should find a solution");
        assertEquals(expectedOptimal, noLayerOptimal.get(), 1e-6, "NoLayer solver should find the same optimal value");
    }
}
