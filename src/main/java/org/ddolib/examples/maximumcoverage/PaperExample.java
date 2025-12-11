package org.ddolib.examples.maximumcoverage;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.ddo.core.heuristics.cluster.GHP;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.util.BitSet;

public class PaperExample {
    public static void main(String[] args) {
        int n = 5; int m = 4; int k = 3;
        BitSet[] ss = new BitSet[m];
        ss[0] = new BitSet(n);  ss[0].set(0);  ss[0].set(2);
        ss[1] = new BitSet(n);  ss[1].set(1);  ss[1].set(3); ss[1].set(3);
        ss[2] = new BitSet(n);  ss[2].set(3);  /*ss[2].set(1);*/
        ss[3] = new BitSet(n);  ss[3].set(1);  ss[3].set(4);
//        ss[4] = new BitSet(n);  ss[4].set(1);  ss[4].set(3);
        MaxCoverProblem problem = new MaxCoverProblem(n, m, k, ss);
        System.out.println(problem);
        DdoModel<MaxCoverState> model = new DdoModel<>() {
            @Override
            public Problem<MaxCoverState> problem() {
                return problem;
            }

            @Override
            public MaxCoverRelax relaxation() {
                return new MaxCoverRelax(problem);
            }

            @Override
            public MaxCoverRanking ranking() {
                return new MaxCoverRanking();
            }

            @Override
            public WidthHeuristic<MaxCoverState> widthHeuristic() {
                return new FixedWidth<>(2);
            }

            @Override
            public boolean exportDot() {
                return true;
            }

            @Override
            public ReductionStrategy<MaxCoverState> relaxStrategy() {
                return new GHP<>(new MaxCoverDistance(problem));
//                return new CostBased<>(new MaxCoverRanking());
                //return new Kmeans<>(new MaxCoverCoordinates(problem));
                // return new Hybrid<>(new MaxCoverRanking(), new MaxCoverDistance(problem));

            }

            @Override
            public ReductionStrategy<MaxCoverState> restrictStrategy() {
                return new GHP<>(new MaxCoverDistance(problem));
//                return new CostBased<>(new MaxCoverRanking());
                // return new Kmeans<>(new MaxCoverCoordinates(problem));
                //return new Hybrid<>(new MaxCoverRanking(), new MaxCoverDistance(problem));
            }
        };

        SearchStatistics stats = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println();
        System.out.println(stats);
    }
}
