package org.ddolib.astar.core.solver;

import org.ddolib.common.solver.Solution;
import org.ddolib.examples.tsp.TSPGenerator;
import org.ddolib.examples.tsp.TSPState;
import org.ddolib.examples.tspnolayer.TSPNoLayerModel;
import org.ddolib.examples.tspnolayer.TSPNoLayerProblem;
import org.ddolib.modeling.nolayer.NoLayerModel;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TSPNoLayerAStarSolverTest {

    @Test
    public void testTSPNoLayerAStarSolver() {
        // Generate a random TSP instance
        TSPGenerator generator = new TSPGenerator(10, 42, 100);
        double[][] distMatrix = generator.distanceMatrix;

        // Solve with the layered API to get optimal
        org.ddolib.examples.tsp.TSPProblem layeredProblem = new org.ddolib.examples.tsp.TSPProblem(distMatrix);
        org.ddolib.modeling.Model<TSPState> layeredModel = new org.ddolib.modeling.Model<>() {
            @Override
            public org.ddolib.modeling.Problem<TSPState> problem() {
                return layeredProblem;
            }
            @Override
            public org.ddolib.modeling.FastLowerBound<TSPState> lowerBound() {
                return new org.ddolib.examples.tsp.TSPFastLowerBound(layeredProblem);
            }
        };
        AStarSolver<TSPState> layeredSolver = new AStarSolver<>(layeredModel);
        layeredSolver.minimize(stats -> false, (sol, stats) -> {});
        double expectedOptimal = layeredSolver.bestValue().orElseThrow();

        // Solve with the new NoLayer API
        TSPNoLayerProblem noLayerProblem = new TSPNoLayerProblem(distMatrix);
        NoLayerModel<TSPState> noLayerModel = new TSPNoLayerModel(noLayerProblem);

        NoLayerAStarSolver<TSPState> noLayerSolver = new NoLayerAStarSolver<>(noLayerModel);
        
        Solution solution = noLayerSolver.minimize(
                stats -> false,
                (sol, stats) -> {}
        );

        Optional<Double> noLayerOptimal = noLayerSolver.bestValue();
        
        assertTrue(noLayerOptimal.isPresent(), "Solver should find a solution");
        assertEquals(expectedOptimal, noLayerOptimal.get(), 1e-6, "NoLayer solver should find the same optimal value");
    }
}
