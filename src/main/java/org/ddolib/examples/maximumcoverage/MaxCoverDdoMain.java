package org.ddolib.examples.maximumcoverage;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.cluster.CostBased;
import org.ddolib.ddo.core.heuristics.cluster.GHP;
import org.ddolib.ddo.core.heuristics.cluster.Kmeans;
import org.ddolib.ddo.core.heuristics.cluster.ReductionStrategy;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.smic.SMICRanking;
import org.ddolib.examples.smic.SMICRelax;
import org.ddolib.examples.smic.SMICState;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class MaxCoverDdoMain {
    public static void main(String[] args) throws IOException {
        /*int n = 4; int m = 3; int k = 3;
        BitSet[] ss = new BitSet[m];
        ss[0] = new BitSet(n);  ss[0].set(0);  ss[0].set(1);
        ss[1] = new BitSet(n);  ss[1].set(2);  ss[1].set(3);
        ss[2] = new BitSet(n);  /*ss[2].set(1);*/  // ss[2].set(3);
        //ss[3] = new BitSet(n);  ss[3].set(3);  ss[3].set(4);
        // MaxCoverProblem problem = new MaxCoverProblem(n, m, k, ss);

        MaxCoverProblem problem = new MaxCoverProblem("src/test/resources/MaxCover/mc_n10_m5_k3_r10_0.txt");
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

//            @Override
//            public MaxCoverFastLowerBound lowerBound() {
//                return new MaxCoverFastLowerBound(problem);
//            }

            @Override
            public boolean exportDot() {
                return true;
            }

            @Override
            public ReductionStrategy<MaxCoverState> relaxStrategy() {
                // return new GHP<>(new MaxCoverDistance());
                return new Kmeans<>(new MaxCoverCoordinates(problem));
            }

            @Override
            public ReductionStrategy<MaxCoverState> restrictStrategy() {
                return new Kmeans<>(new MaxCoverCoordinates(problem));
            }
        };

        SearchStatistics stats = Solvers.relaxedDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        System.out.println();
        System.out.println(stats);
    }

}
