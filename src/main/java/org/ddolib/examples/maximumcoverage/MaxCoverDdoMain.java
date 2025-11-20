package org.ddolib.examples.maximumcoverage;

import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.smic.SMICRanking;
import org.ddolib.examples.smic.SMICRelax;
import org.ddolib.examples.smic.SMICState;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;
import org.ddolib.util.verbosity.VerbosityLevel;

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
        // MaxCoverProblem problem = new MaxCoverProblem("data/MaxCover/mc_n100_m50_k10_r20_0.txt");
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
                return new FixedWidth<>(4);
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
                return new GHP<>(new MaxCoverDistance(problem));
                // return new CostBased<>(ranking());
                // return new Hybrid<>(new MaxCoverRanking(), new MaxCoverDistance(), 0.75);
            }

            @Override
            public ReductionStrategy<MaxCoverState> restrictStrategy() {
                return new Hybrid<>(new MaxCoverRanking(), new MaxCoverDistance(problem), 0.75);
            }

            @Override
            public VerbosityLevel verbosityLevel() {
                return VerbosityLevel.LARGE;
            }
        };

        long start = System.currentTimeMillis();
        SearchStatistics stats = Solvers.relaxedDdo(model, x-> (System.currentTimeMillis() - start >= 60*1000) ,(sol, s) -> {
            SolutionPrinter.printSolution(s,sol);
        });
        // SearchStatistics stats = Solvers.minimizeExact(model);
        System.out.println();
        System.out.println(stats);
    }

}
