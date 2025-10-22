package org.ddolib.examples.boundedknapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.Model;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

/**
 * Bounded Knapsack Problem (BKS) with AsTar.
 */
public class BKSAstarMain {

    public static void main(String[] args) {
         Model<Integer> model = new Model<>() {
            final BKSProblem problem = new BKSProblem(10, 1000, BKSProblem.InstanceType.STRONGLY_CORRELATED, 0);
            @Override
            public BKSProblem problem() {
                return problem;
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

        SearchStatistics stats = Solvers.minimizeAstar(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}

