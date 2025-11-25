package org.ddolib.examples.maximumcoverage;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.RelaxSearchStatistics;
import org.ddolib.common.solver.RestrictSearchStatistics;
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

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Math.ceil;

public class MaxCoverXPs {

    public static MaxCoverProblem[] generateInstances() {
        int[] ns = {100, 150, 200};
        double[] mFactors = {0.5, 0.8};
        double[] kFactors = {0.1, 0.2};
        double[] maxRs = {0.1, 0.2};
        int nbSeeds = 10;

        int nbInstances = ns.length*mFactors.length*kFactors.length*maxRs.length*nbSeeds;
        MaxCoverProblem[] instances = new MaxCoverProblem[nbInstances];
        int index = 0;
        for (int n: ns) {
            for (double mFactor: mFactors) {
                int m = (int) ceil(mFactor * n);
                for (double kFactor: kFactors) {
                    int k = (int) ceil(kFactor * m);
                    for (double maxR: maxRs) {
                        for (int seed = 0; seed < nbSeeds; seed++) {
                           MaxCoverProblem problem = new MaxCoverProblem(n, m, k, maxR, seed);
                           instances[index] = problem;
                           index++;
                        }
                    }
                }
            }
        }
        return instances;
    }

    private static DdoModel<MaxCoverState> getModel(MaxCoverProblem problem,
                                                    int maxWidth,
                                                    ClusterType clusterType,
                                                    long seed,
                                                    int kmeansIter,
                                                    double hybridFactor) {
        return new DdoModel<>() {
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
                return new FixedWidth<>(maxWidth);
            }

            @Override
            public boolean exportDot() {
                return false;
            }

            @Override
            public StateDistance<MaxCoverState> stateDistance() {
                return new MaxCoverDistance(problem);
            }

            @Override
            public ReductionStrategy<MaxCoverState> relaxStrategy() {
                ReductionStrategy<MaxCoverState> strat = null;
                switch (clusterType) {
                    case Cost -> strat = new CostBased<>(new MaxCoverRanking());
                    case GHP -> strat = new GHP<>(stateDistance(), seed);
                    case Kmeans -> strat = new Kmeans<>(new MaxCoverCoordinates(problem), kmeansIter);
                    case Hybrid -> strat = new Hybrid<>(new MaxCoverRanking(), stateDistance(), hybridFactor, seed);
                    case Random -> strat = new RandomBased<>(seed);
                }
                return strat;
            }

            @Override
            public ReductionStrategy<MaxCoverState> restrictStrategy() {
                return relaxStrategy();
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
        };
    }

    private static void xpRelaxation() throws IOException {
        MaxCoverProblem[] instances = generateInstances();
        FileWriter writer = new FileWriter("xps/relaxationsMaxCover.csv");
        writer.write("Instance;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;" +
                "isExact;RunTime(ms);Incumbent;NbRelaxations;avgExactNodes;minExactNodes;maxExactNodes;avgMinCardinality;avgMaxCardinality;"+
                "avgAvgCardinality;avgMinDegradation;avgMaxDegradation;avgAvgDegradation\n");

        for (MaxCoverProblem problem : instances) {
            for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
                for (ClusterType clusterType : new ClusterType[]{ClusterType.Cost, ClusterType.GHP, ClusterType.Hybrid}) {
                    int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5, 10, 50};
                    long[] ghpSeeds = clusterType != ClusterType.GHP ? new long[]{465465} : new long[]{465465, 546351, 87676};
                    double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
                    for (long seed : ghpSeeds) {
                        for (int kmeansIter : kmeansIters) {
                            for (double hybridFactor : hybridFactors) {
                                DdoModel<MaxCoverState> model = getModel(problem,
                                        maxWidth,
                                        clusterType,
                                        seed,
                                        kmeansIter,
                                        hybridFactor);
                                assert problem.name.isPresent();
                                System.out.printf("%s %d %d %d %f %n", problem.name.get(), maxWidth, kmeansIter, seed, hybridFactor);
                                RelaxSearchStatistics stats = Solvers.relaxedDdo(model);

                                writer.append(String.format("%s;%s;%d;%d;%d;%f;%s%n",
                                        problem.name.get(),
                                        clusterType,
                                        maxWidth,
                                        seed,
                                        kmeansIter,
                                        hybridFactor,
                                        stats
                                ));
                                writer.flush();
                            }
                        }
                    }
                }
            }
        }
        writer.close();
    }

    private static void xpRestriction() throws IOException {
        MaxCoverProblem[] instances = generateInstances();
        FileWriter writer = new FileWriter("xps/restrictionMaxCover.csv");
        writer.write("Instance;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;" +
                "isExact;RunTime(ms);Incumbent;NbRestrictions;AvgLayerSize\n");

        for (MaxCoverProblem problem : instances) {
            for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
                for (ClusterType clusterType : new ClusterType[]{ClusterType.Cost, ClusterType.GHP, ClusterType.Hybrid, ClusterType.Random}) {
                    int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5, 10, 50};
                    long[] ghpSeeds = (clusterType != ClusterType.GHP) && (clusterType != ClusterType.Random) ? new long[]{465465} : new long[]{465465, 546351, 87676};
                    double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
                    for (long seed : ghpSeeds) {
                        for (int kmeansIter : kmeansIters) {
                            for (double hybridFactor : hybridFactors) {
                                DdoModel<MaxCoverState> model = getModel(problem,
                                        maxWidth,
                                        clusterType,
                                        seed,
                                        kmeansIter,
                                        hybridFactor);
                                assert problem.name.isPresent();
                                System.out.printf("%s %s %d %d %d %f %n", problem.name.get(), clusterType, maxWidth, kmeansIter, seed, hybridFactor);
                                RestrictSearchStatistics stats = Solvers.restrictedDdo(model);

                                writer.append(String.format("%s;%s;%d;%d;%d;%f;%s%n",
                                        problem.name.get(),
                                        clusterType,
                                        maxWidth,
                                        seed,
                                        kmeansIter,
                                        hybridFactor,
                                        stats
                                ));
                                writer.flush();
                            }
                        }
                    }
                }
            }
        }
        writer.close();
    }

    public static void main(String[] args) {
        try {
            xpRelaxation();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }


    }

    private enum ClusterType {
        Cost,
        GHP,
        Kmeans,
        Hybrid,
        Random
    }


}
