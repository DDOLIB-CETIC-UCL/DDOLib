package org.ddolib.examples.boundedknapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.ddo.core.profiling.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Solve;

/**
 * Bounded Knapsack Problem (BKS)
 * A bounded knapsack problem is a variation of the classic knapsack problem
 * where each item can be included in the knapsack a limited number of times.
 */
public class BKSAcsMain {

    public static void main(String[] args) {
        BoundedKnapsackGenerator generator = new BoundedKnapsackGenerator(10, 1000, BoundedKnapsackGenerator.InstanceType.STRONGLY_CORRELATED, 0);

        AcsModel<Integer> model = new AcsModel<>(){
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
            public BKSFastLowerBound lowerBound() {
                return new BKSFastLowerBound(problem);
            }
            @Override
            public DominanceChecker<Integer> dominance() {
                return new SimpleDominanceChecker<Integer>(new BKSDominance(), problem.nbVars());
            }
        };

        Solve<Integer> solve = new Solve<>();

        SearchStatistics stats = solve.minimizeAcs(model);

        solve.onSolution(stats);
    }
}

