package org.ddolib.examples.boundedknapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.modeling.AcsModel;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

/**
 * Bounded Knapsack Problem (BKS) with Acs.
 */
public class BKSAcsMain {

    public static void main(String[] args) {
        final BKSProblem problem = new BKSProblem(10, 1000, BKSProblem.InstanceType.STRONGLY_CORRELATED, 0);
        AcsModel<Integer> model = new AcsModel<>() {
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

        SearchStatistics stats = Solvers.minimizeAcs(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}

