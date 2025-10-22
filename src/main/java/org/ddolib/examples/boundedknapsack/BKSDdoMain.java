package org.ddolib.examples.boundedknapsack;

import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

/**
 * Bounded Knapsack Problem (BKS) with Ddo.
 */
public class BKSDdoMain {

    public static void main(String[] args) {
        final BKSProblem problem = new BKSProblem(100, 1000, BKSProblem.InstanceType.STRONGLY_CORRELATED, 0);
        DdoModel<Integer> model = new DdoModel<>() {
            @Override
            public BKSProblem problem() {
                return problem;
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

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });

        System.out.println(stats);
    }
}

