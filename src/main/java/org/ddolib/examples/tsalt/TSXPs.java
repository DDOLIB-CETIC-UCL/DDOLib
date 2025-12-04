package org.ddolib.examples.tsalt;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.RelaxSearchStatistics;
import org.ddolib.common.solver.RestrictSearchStatistics;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.knapsack.*;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class TSXPs {

    private static TSProblem[] loadInstances() throws IOException {
        String instancePath = Path.of("data", "TalentScheduling", "or-library", "kept").toString();
        File instanceDir = new File(instancePath);
        File[] files = instanceDir.listFiles();
        TSProblem[] problems = new TSProblem[files.length ];
        for (int i = 0; i < files.length; i++) {
            problems[i] = new TSProblem(files[i].getAbsolutePath());
        }
        return problems;
    }

    private static DdoModel<TSState> getModel(TSProblem problem,
                                              int maxWidth,
                                              ClusterType clusterType,
                                              long seed,
                                              int kmeansIter,
                                              double hybridFactor) {
        return getModel(problem, maxWidth, clusterType, clusterType, seed, kmeansIter, hybridFactor);
    }

    private static DdoModel<TSState> getModel(TSProblem problem,
                                              int maxWidth,
                                              ClusterType relaxType,
                                              ClusterType restrictType,
                                              long seed,
                                              int kmeansIter,
                                              double hybridFactor) {
        return new DdoModel<>() {
            @Override
            public Problem<TSState> problem() {
                return problem;
            }

            @Override
            public TSRelax relaxation() {
                return new TSRelax(problem);
            }

            @Override
            public TSRanking ranking() {
                return new TSRanking();
            }

            @Override
            public WidthHeuristic<TSState> widthHeuristic() {
                return new FixedWidth<>(maxWidth);
            }

            @Override
            public boolean exportDot() {
                return false;
            }

            @Override
            public StateDistance<TSState> stateDistance() {
                return new TSDistance(problem);
            }

            @Override
            public ReductionStrategy<TSState> relaxStrategy() {
                ReductionStrategy<TSState> strat = null;
                switch (relaxType) {
                    case Cost -> strat = new CostBased<>(new TSRanking());
                    case GHP -> strat = new GHP<>(stateDistance(), seed);
                    case Kmeans -> strat = new Kmeans<>(new TSCoordinates(problem), kmeansIter, false);
                    case Hybrid -> strat = new Hybrid<>(new TSRanking(), stateDistance(), hybridFactor, seed);
                    case Random -> strat = new RandomBased<>(seed);
                }
                return strat;
            }

            @Override
            public ReductionStrategy<TSState> restrictStrategy() {
                ReductionStrategy<TSState> strat = null;
                switch (restrictType) {
                    case Cost -> strat = new CostBased<>(new TSRanking());
                    case GHP -> strat = new GHP<>(stateDistance(), seed);
                    case Kmeans -> strat = new Kmeans<>(new TSCoordinates(problem), kmeansIter, false);
                    case Hybrid -> strat = new Hybrid<>(new TSRanking(), stateDistance(), hybridFactor, seed);
                    case Random -> strat = new RandomBased<>(seed);
                }
                return strat;
            }

            @Override
            public DominanceChecker<TSState> dominance() {
                return new DefaultDominanceChecker<>();
            }

            @Override
            public Frontier<TSState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.LastExactLayer);
            }

            @Override
            public FastLowerBound<TSState> lowerBound() {
                return new TSFastLowerBound(problem);
            }

            @Override
            public boolean useCache() {
                return false;
            }
        };
    }

    private static void xpRelaxation() throws IOException {
        TSProblem[] instances = loadInstances();
        FileWriter writer = new FileWriter("xps/relaxationsTS.csv");
        writer.write("Instance;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;" +
                "isExact;RunTime(ms);Incumbent;NbRelaxations" +
                ";avgExactNodes;geoAvgExactNodes;minExactNodes;maxExactNodes;varExactNodes" +
                ";avgStateCardinalities;geoAvgStateCardinalities;minStateCardinalities;maxStateCardinalities;varStateCardinalities" +
                ";avgStateDegradation;geoAvgStateDegradation;minStateDegradation;maxStateDegradation;varStateDegradation" +
                ";avgLayerSize;geoAvgLayerSize;minLayerSize;maxLayerSize;varLayerSize;nbNode;nbExactNode" +
                ";\n");

        for (TSProblem problem : instances) {
            for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
                for (ClusterType clusterType : new ClusterType[]{ClusterType.GHP, ClusterType.Cost, ClusterType.Kmeans}) {
                    int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5};
                    long[] ghpSeeds = clusterType != ClusterType.GHP ? new long[]{465465} : new long[]{465465, 546351, 87676};
                    double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.2, 0.4, 0.6, 0.8};
                    for (long seed : ghpSeeds) {
                        for (int kmeansIter : kmeansIters) {
                            for (double hybridFactor : hybridFactors) {
                                DdoModel<TSState> model = getModel(problem,
                                        maxWidth,
                                        clusterType,
                                        seed,
                                        kmeansIter,
                                        hybridFactor);
                                assert problem.name.isPresent();
                                double optimal = problem.optimal.isPresent() ? problem.optimal.get() : -1;
                                System.out.printf("%s %f %d %d %d %f %n", problem.name.get(), optimal, maxWidth, kmeansIter, seed, hybridFactor);
                                RelaxSearchStatistics stats = Solvers.relaxedDdo(model);

                                writer.append(String.format("%s;%f;%s;%d;%d;%d;%f;%s%n",
                                        problem.name.get(),
                                        optimal,
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
        TSProblem[] instances = loadInstances();
        FileWriter writer = new FileWriter("xps/restrictionTS.csv");
        writer.write("Instance;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;" +
                "isExact;RunTime(ms);Incumbent;NbRestrictions;AvgLayerSize\n");

        for (TSProblem problem : instances) {
            for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
                for (ClusterType clusterType : new ClusterType[]{ClusterType.GHP, ClusterType.Cost, ClusterType.Kmeans}) {
                    int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5};
                    long[] ghpSeeds = (clusterType != ClusterType.GHP) && (clusterType != ClusterType.Random) ? new long[]{465465} : new long[]{546351, 87676, 465465};
                    double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.2, 0.4, 0.6, 0.8};
                    for (long seed : ghpSeeds) {
                        for (int kmeansIter : kmeansIters) {
                            for (double hybridFactor : hybridFactors) {
                                DdoModel<TSState> model = getModel(problem,
                                        maxWidth,
                                        clusterType,
                                        seed,
                                        kmeansIter,
                                        hybridFactor);
                                assert problem.name.isPresent();
                                double optimal = problem.optimal.isPresent() ? problem.optimal.get() : -1;
                                System.out.printf("%s %f %d %d %d %f %n", problem.name.get(), optimal, maxWidth, kmeansIter, seed, hybridFactor);
                                RestrictSearchStatistics stats = Solvers.restrictedDdo(model);

                                writer.append(String.format("%s;%f;%s;%d;%d;%d;%f;%s%n",
                                        problem.name.get(),
                                        optimal,
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

    public static void main(String[] args) throws IOException {
        xpRelaxation();
        xpRestriction();
    }

    private enum ClusterType {
        Cost,
        GHP,
        Kmeans,
        Hybrid,
        Random
    }


}
