package org.ddolib.examples.boundedknapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Solver;

/**
 * Bounded Knapsack Problem (BKS)
 * A bounded knapsack problem is a variation of the classic knapsack problem
 * where each item can be included in the knapsack a limited number of times.
 */
public class BKSAstarMain {

    public static void main(String[] args) {
        BoundedKnapsackGenerator generator = new BoundedKnapsackGenerator(10, 1000, BoundedKnapsackGenerator.InstanceType.STRONGLY_CORRELATED, 0);

        Model<Integer> model = new Model<>() {
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

        Solver<Integer> solver = new Solver<>();

        SearchStatistics stats = solver.minimizeAstar(model);

        System.out.println(stats);
    }
}

