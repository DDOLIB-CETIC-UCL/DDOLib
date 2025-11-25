package org.ddolib.examples.maximumcoverage;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.RelaxSearchStatistics;
import org.ddolib.common.solver.SearchStatistics;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;
import org.ddolib.util.io.SolutionPrinter;

import java.io.IOException;
import java.util.DoubleSummaryStatistics;

public class MaxCoverDdoMainTestReladedAtRoot {
    public static void main(String[] args) throws IOException {

        DoubleSummaryStatistics statsCost = new DoubleSummaryStatistics();
        DoubleSummaryStatistics statsCluster = new DoubleSummaryStatistics();

        // for (int i = 138; i < 139; i++) {
        for (int i = 0; i < 139; i++) {
            MaxCoverProblem problem = new MaxCoverProblem(10, 6, 4,0.1,i);

            System.out.println(problem);
            double lb1 = lowerBound(problem,false); //lowSystem.out.println(problem);`
            statsCost.accept(lb1);
            double lb2 = lowerBound(problem,true); //lowSystem.out.println(problem);`
            statsCluster.accept(lb2);

            if (lb1 - lb2 > 2.5) {
                System.out.println(i);
            }



        }

        System.out.println("Cost Based Strategy Stats:");

        System.out.println(statsCost);

        System.out.println("Cluster Strategy Stats:");
        System.out.println(statsCluster);


    }


    public static double lowerBound(MaxCoverProblem problem, boolean cluster) throws IOException {
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
                return new FixedWidth<>(5);
            }

            @Override
            public boolean exportDot() {
                return true;
            }

            @Override
            public ReductionStrategy<MaxCoverState> relaxStrategy() {
                if (cluster) {
                    //return new Hybrid<>(new MaxCoverRanking(), new MaxCoverDistance(), 0.1 );
                    GHPAlt ghp =  new GHPAlt(new MaxCoverDistance(problem), new MaxCoverRelax(problem), problem);
                    // GHP ghp = new GHP(new MaxCoverDistance(problem));
                    ghp.setSeed(42);
                    return ghp;
                    //return new GHP<>(new MaxCoverDistance());
                } else {
                    return new CostBased<>(new MaxCoverRanking());
                }
            }

            @Override
            public DominanceChecker<MaxCoverState> dominance() {
                return new DefaultDominanceChecker<>();
                //return new SimpleDominanceChecker<>(new MaxCoverDominance(), problem.nbVars());
            }

            @Override
            public Frontier<MaxCoverState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.LastExactLayer);
            }

            @Override
            public FastLowerBound<MaxCoverState> lowerBound() {
                return new MaxCoverFastLowerBound(problem);
            }

            @Override
            public boolean useCache() {
                return false;
            }

            @Override
            public ReductionStrategy<MaxCoverState> restrictStrategy() {
                return new Kmeans<>(new MaxCoverCoordinates(problem));
            }
        };

        RelaxSearchStatistics stats = Solvers.relaxedDdo(model, (sol, s) -> {
            SolutionPrinter.printSolution(s, sol);
        });

        return stats.incumbent();
    }

}
