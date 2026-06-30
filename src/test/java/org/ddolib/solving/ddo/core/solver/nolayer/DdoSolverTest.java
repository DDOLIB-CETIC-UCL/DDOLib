package org.ddolib.solving.ddo.core.solver.nolayer;

import org.ddolib.common.solver.layered.Solution;
import org.ddolib.examples.nolayer.knapsack.KSProblem;
import org.ddolib.examples.nolayer.knapsack.KSState;
import org.ddolib.examples.nolayer.knapsack.KSModel;
import org.ddolib.modeling.layered.StateRanking;
import org.ddolib.modeling.nolayer.DdoModel;
import org.ddolib.modeling.nolayer.Relaxation;
import org.ddolib.solving.ddo.core.heuristics.cluster.nolayer.CostBased;
import org.ddolib.solving.ddo.core.heuristics.cluster.nolayer.ReductionStrategy;
import org.ddolib.solving.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.solving.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.util.verbosity.VerbosityLevel;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DdoSolverTest {

    @Test
    public void testKSNoLayerDdoSolver() {
        // Generate a simple KS instance
        int[] weights = {2, 3, 4, 5, 9};
        int[] profits = {3, 4, 8, 8, 10};
        int capacity = 20;

        // Solve with layered API to get optimal
        org.ddolib.examples.layered.knapsack.KSProblem layeredProblem = new org.ddolib.examples.layered.knapsack.KSProblem(capacity, profits, weights);
        org.ddolib.modeling.layered.Model<Integer> layeredModel = new org.ddolib.modeling.layered.Model<Integer>() {
            @Override
            public org.ddolib.modeling.layered.Problem<Integer> problem() {
                return layeredProblem;
            }

            @Override
            public org.ddolib.modeling.layered.FastLowerBound<Integer> lowerBound() {
                return new org.ddolib.examples.layered.knapsack.KSFastLowerBound(layeredProblem);
            }
        };
        org.ddolib.solving.astar.core.solver.layered.AStarSolver<Integer> layeredSolver = new org.ddolib.solving.astar.core.solver.layered.AStarSolver<>(layeredModel);
        layeredSolver.minimize(stats -> false, (sol, stats) -> {
        });
        double expectedOptimal = layeredSolver.bestValue().orElseThrow();

        // Solve with new NoLayer API
        KSProblem noLayerProblem = new KSProblem(profits, weights, capacity);
        DdoModel<KSState> noLayerModel = new DdoModel<KSState>() {
            KSModel base = new KSModel(noLayerProblem);

            @Override
            public org.ddolib.modeling.nolayer.Problem<KSState> problem() {
                return base.problem();
            }

            @Override
            public org.ddolib.modeling.nolayer.FastLowerBound<KSState> lowerBound() {
                return base.lowerBound();
            }

            @Override
            public Relaxation<KSState> relaxation() {
                return states -> {
                    Iterator<KSState> it = states.iterator();
                    KSState first = it.next();
                    int maxCap = first.remainingCapacity();
                    int maxItem = first.currentItem();
                    while (it.hasNext()) {
                        KSState s = it.next();
                        maxCap = Math.max(maxCap, s.remainingCapacity());
                        maxItem = Math.max(maxItem, s.currentItem());
                    }
                    return new KSState(maxItem, maxCap);
                };
            }

            @Override
            public StateRanking<KSState> ranking() {
                return (s1, s2) -> Integer.compare(s2.remainingCapacity(), s1.remainingCapacity());
            }

            @Override
            public WidthHeuristic<KSState> widthHeuristic() {
                return new FixedWidth<>(2);
            }

            @Override
            public ReductionStrategy<KSState> relaxStrategy() {
                return new CostBased<>(ranking());
            }

            @Override
            public ReductionStrategy<KSState> restrictStrategy() {
                return new CostBased<>(ranking());
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.SILENT;
            }

            @Override
            public boolean useCache() {
                return false;
            }
        };

        DdoSolver<KSState> noLayerSolver = new DdoSolver<>(noLayerModel);
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
