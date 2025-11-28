package org.ddolib.examples.knapsack;

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
import org.ddolib.examples.maximumcoverage.MaxCoverProblem;
import org.ddolib.examples.maximumcoverage.MaxCoverState;
import org.ddolib.examples.maximumcoverage.MaxCoverXPs;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class KSXPs {

    protected static KSProblem[] loadInstances() throws IOException {
        String instancePath = Path.of("data", "Knapsack", "Nafar_2024").toString();
        System.out.println(instancePath);
        File instanceDir = new File(instancePath);
        String pattern = "KP_\\d*.txt"; // regex

        File[] files = instanceDir.listFiles(f -> f.isFile() && f.getName().matches(pattern));
        KSProblem[] problems = new KSProblem[files.length];
        for (int i = 0; i < files.length; i++) {
            problems[i] = new KSProblem(files[i].getAbsolutePath());
        }

        return problems;
    }

    private static DdoModel<Integer> getModel(KSProblem problem,
                                                    int maxWidth,
                                                    ClusterType clusterType,
                                                    long seed,
                                                    int kmeansIter,
                                                    double hybridFactor) {
        return getModel(problem, maxWidth, clusterType, clusterType, seed, kmeansIter, hybridFactor);
    }

    private static DdoModel<Integer> getModel(KSProblem problem,
                                                    int maxWidth,
                                                    ClusterType relaxType,
                                                    ClusterType restrictType,
                                                    long seed,
                                                    int kmeansIter,
                                                    double hybridFactor) {
        return new DdoModel<>() {
            @Override
            public Problem<Integer> problem() {
                return problem;
            }

            @Override
            public KSRelax relaxation() {
                return new KSRelax();
            }

            @Override
            public KSRanking ranking() {
                return new KSRanking();
            }

            @Override
            public WidthHeuristic<Integer> widthHeuristic() {
                return new FixedWidth<>(maxWidth);
            }

            @Override
            public boolean exportDot() {
                return false;
            }

            @Override
            public StateDistance<Integer> stateDistance() {
                return new KSDistance(problem);
            }

            @Override
            public ReductionStrategy<Integer> relaxStrategy() {
                ReductionStrategy<Integer> strat = null;
                switch (relaxType) {
                    case Cost -> strat = new CostBased<>(new KSRanking());
                    case GHP -> strat = new GHP<>(stateDistance(), seed);
                    case Kmeans -> strat = new Kmeans<>(new KSCoordinates(), kmeansIter, false);
                    case Hybrid -> strat = new Hybrid<>(new KSRanking(), stateDistance(), hybridFactor, seed);
                    case Random -> strat = new RandomBased<>(seed);
                }
                return strat;
            }

            @Override
            public ReductionStrategy<Integer> restrictStrategy() {
                ReductionStrategy<Integer> strat = null;
                switch (restrictType) {
                    case Cost -> strat = new CostBased<>(new KSRanking());
                    case GHP -> strat = new GHP<>(stateDistance(), seed);
                    case Kmeans -> strat = new Kmeans<>(new KSCoordinates(), kmeansIter, false);
                    case Hybrid -> strat = new Hybrid<>(new KSRanking(), stateDistance(), hybridFactor, seed);
                    case Random -> strat = new RandomBased<>(seed);
                }
                return strat;
            }

            @Override
            public DominanceChecker<Integer> dominance() {
                return new DefaultDominanceChecker<>();
            }

            @Override
            public Frontier<Integer> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.LastExactLayer);
            }

            @Override
            public FastLowerBound<Integer> lowerBound() {
                return new KSFastLowerBound(problem);
            }

            @Override
            public boolean useCache() {
                return false;
            }
        };
    }

    private static void xpRelaxation() throws IOException {
        KSProblem[] instances = loadInstances();
        FileWriter writer = new FileWriter("xps/relaxationsKS.csv");
        writer.write("Instance;Optimal;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;" +
                "isExact;RunTime(ms);Incumbent;NbRelaxations;avgExactNodes;minExactNodes;maxExactNodes;avgMinCardinality;avgMaxCardinality;"+
                "avgAvgCardinality;avgMinDegradation;avgMaxDegradation;avgAvgDegradation;AvgDegradation;MinDegradation;MaxDegradation\n");

        for (KSProblem problem : instances) {
            for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
                for (ClusterType clusterType : new ClusterType[]{ClusterType.Cost, ClusterType.GHP, ClusterType.Hybrid, ClusterType.Kmeans}) {
                    int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5, 10, 50};
                    long[] ghpSeeds = clusterType != ClusterType.GHP ? new long[]{465465} : new long[]{465465, 546351, 87676};
                    double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
                    for (long seed : ghpSeeds) {
                        for (int kmeansIter : kmeansIters) {
                            for (double hybridFactor : hybridFactors) {
                                DdoModel<Integer> model = getModel(problem,
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
        KSProblem[] instances = loadInstances();
        FileWriter writer = new FileWriter("xps/restrictionKS.csv");
        writer.write("Instance;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;" +
                "isExact;RunTime(ms);Incumbent;NbRestrictions;AvgLayerSize\n");

        for (KSProblem problem : instances) {
            for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
                for (ClusterType clusterType : new ClusterType[]{ ClusterType.Random,}) {// ClusterType.Cost, ClusterType.GHP, ClusterType.Hybrid, ClusterType.Kmeans,}) {
                    int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5, 10, 50};
                    long[] ghpSeeds = (clusterType != ClusterType.GHP) && (clusterType != ClusterType.Random) ? new long[]{465465} : new long[]{546351, 87676};
                    double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
                    for (long seed : ghpSeeds) {
                        for (int kmeansIter : kmeansIters) {
                            for (double hybridFactor : hybridFactors) {
                                DdoModel<Integer> model = getModel(problem,
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

    private static void xpBnB(String instance) throws IOException {
        KSProblem problem = new KSProblem(instance);
        FileWriter writer = new FileWriter("results/");
        // writer.write("Instance;RelaxType;RestrictType;MaxWidth;Seed;KmeansIter;HybridFactor;" +
        //        "Status;nbIterations;queueMaxSize;RunTimeMs(ms);Incumbent;Gap\n");

        int maxWidth = 60;
        int kmeansIter = -1;
        double hybridFactor = -1;
        ClusterType[] relaxTypes = new ClusterType[]{ClusterType.Cost, ClusterType.GHP, ClusterType.Kmeans};
        ClusterType[] restrictTypes = new ClusterType[]{ClusterType.Cost, ClusterType.GHP, ClusterType.Random, ClusterType.Kmeans};
        for (ClusterType relaxType: relaxTypes) {
            for (ClusterType restrictType : restrictTypes) {
                long[] seeds = (relaxType == ClusterType.GHP || restrictType == ClusterType.GHP || restrictType == ClusterType.Random) ? new long[]{465465} : new long[]{465465, 546351, 87676};
                for (long seed : seeds) {
                    DdoModel<Integer> model = getModel(problem,
                            maxWidth,
                            relaxType,
                            restrictType,
                            seed,
                            kmeansIter,
                            hybridFactor);
                    assert problem.name.isPresent();
                    System.out.printf("%s %s %d %d %d %f %n", problem.name.get(), restrictType, maxWidth, kmeansIter, seed, hybridFactor);
                    long startTime = System.currentTimeMillis();
                    SearchStatistics stats = Solvers.minimizeDdo(model, x -> (System.currentTimeMillis() - startTime >= 1000.0 * 300.0));

                    writer.append(String.format("%s;%s;%s;%d;%d;%d;%f;%s%n",
                            problem.name.get(),
                            relaxType,
                            restrictType,
                            maxWidth,
                            seed,
                            kmeansIter,
                            hybridFactor,
                            stats.toCSV()
                    ));
                    writer.flush();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            xpBnB(args[0]);
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
