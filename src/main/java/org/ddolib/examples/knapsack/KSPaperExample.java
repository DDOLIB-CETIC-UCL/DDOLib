package org.ddolib.examples.knapsack;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.ddo.core.heuristics.cluster.GHP;
import org.ddolib.ddo.core.heuristics.cluster.Kmeans;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.*;
import org.ddolib.util.io.SolutionPrinter;

public class KSPaperExample {
    public static void main(String[] args) {
        final KSProblem problem = new KSProblem(12, new int[]{5,4,3,6,8}, new int[]{3,5,6,4,5});
        final DdoModel<Integer> model = new DdoModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public Relaxation<Integer> relaxation() {
                return new KSRelax();
            }

            @Override
            public KSRanking ranking() {
                return new KSRanking();
            }

            @Override
            public boolean exportDot() {
                return true;
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(3);
            }

            @Override
            public ReductionStrategy<Integer> relaxStrategy() {
                //return new GHP<>(new KSDistance());
                //return new CostBased<>(new KSRanking());
                return new Kmeans<>(new KSCoordinates());
                // return new Hybrid<>(new KSRanking(), new KSDistance(problem));
            }

            @Override
            public ReductionStrategy<Integer> restrictStrategy() {
                //return new GHP<>(new KSDistance());
                //return new CostBased<>(new KSRanking());
                return new Kmeans<>(new KSCoordinates());
                //return new Hybrid<>(new KSRanking(), new KSDistance(problem));
            }
        };

        Solution solution = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        System.out.println(solution);

    }
}
