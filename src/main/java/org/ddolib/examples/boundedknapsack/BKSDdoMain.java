package org.ddolib.examples.boundedknapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solver;
import org.ddolib.common.solver.SolverConfig;
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

import java.util.Arrays;
import java.util.Random;

/**
 * Bounded Knapsack Problem (BKS)
 * A bounded knapsack problem is a variation of the classic knapsack problem
 * where each item can be included in the knapsack a limited number of times.
 */
public class BKSDdoMain {

    public static void main(String[] args) {
        BoundedKnapsackGenerator generator = new BoundedKnapsackGenerator(10, 1000, BoundedKnapsackGenerator.InstanceType.STRONGLY_CORRELATED, 0);

        DdoModel<Integer> model = new DdoModel<>(){
            private BKSProblem problem;
            @Override
            public BKSProblem problem() {
                try {
                    problem = new BKSProblem(generator.generate());
                    return problem;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public BKSRelax relaxation() {
                return new BKSRelax();
            }
            @Override
            public BKSRanking ranking() {
                return new BKSRanking();
            }
            @Override
            public BKSFastLowerBound lowerBound() {
                return new BKSFastLowerBound(problem);
            }
            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<Integer>(new BKSDominance(), problem.nbVars());
            }
            @Override
            public boolean useCache() {
                return true;
            }
            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(100);
            }
            @Override
            public SimpleFrontier<Integer> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.Frontier);
            }
        };

        Solve<Integer> solve = new Solve<>();

        SearchStatistics stats = solve.minimizeDdo(model);

        solve.onSolution(stats);
    }
}

