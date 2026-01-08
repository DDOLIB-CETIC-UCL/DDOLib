package org.ddolib.examples.maximumcoverage;

import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.ddo.core.heuristics.cluster.GHP;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;

public class MaxCoverDdoMainWithCluster {
    public static void main(String[] args) throws IOException {
        MaxCoverProblem problem = new MaxCoverProblem(30, 30, 7,0.1,42);
        // MaxCoverProblem problem = new MaxCoverProblem(10, 10, 5,0.1,42);

        // MaxCoverProblem problem = new MaxCoverProblem("src/test/resources/MaxCover/mc_n10_m5_k3_r10_0.txt");
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
                return new FixedWidth<>(100);
            }

            @Override
            public MaxCoverFastLowerBound lowerBound() {
                return new MaxCoverFastLowerBound(problem);
            }

            @Override
            public boolean exportDot() {
                return false;
            }

            @Override
            public ReductionStrategy<MaxCoverState> relaxStrategy() {
                return new GHP<>(new MaxCoverDistance(problem));
//                return new Hybrid<>(new MaxCoverRanking(), new MaxCoverDistance(problem));
//                return new CostBased<>(new MaxCoverRanking()); // default strategy
            }

            @Override
            public ReductionStrategy<MaxCoverState> restrictStrategy() {
                return new GHP<>(new MaxCoverDistance(problem));
//                return new Hybrid<>(new MaxCoverRanking(), new MaxCoverDistance(problem));
//                return new CostBased<>(new MaxCoverRanking()); // default strategy
            }
        };

        Solution solution = Solvers.minimizeDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println();
        System.out.println(solution);
    }

}
