package org.ddolib.examples.mks;

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
import org.ddolib.examples.knapsack.KSProblem;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.FastLowerBound;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static java.lang.Math.ceil;

public class MKSXPs {

    public static MKSProblem[] loadInstances() throws IOException {
        String instancePath = Path.of("data", "MKS", "or-library").toString();
        System.out.println(instancePath);
        File instanceDir = new File(instancePath);
        // String pattern = "MKP_\\d*.txt";
        String pattern = "mknapcb\\d*_\\d*.txt"; // regex

        File[] files = instanceDir.listFiles(f -> f.isFile() && f.getName().matches(pattern));
        MKSProblem[] problems = new MKSProblem[files.length];
        for (int i = 0; i < files.length; i++) {
            problems[i] = new MKSProblem(files[i].getAbsolutePath());
        }

        return problems;
    }

    private static DdoModel<MKSState> getModel(MKSProblem problem,
                                                    int maxWidth,
                                                    ClusterType clusterType,
                                                    long seed,
                                                    int kmeansIter,
                                                    double hybridFactor) {
        return getModel(problem, maxWidth, clusterType, clusterType, seed, kmeansIter, hybridFactor);
    }

    private static DdoModel<MKSState> getModel(MKSProblem problem,
                                                    int maxWidth,
                                                    ClusterType relaxType,
                                                    ClusterType restrictType,
                                                    long seed,
                                                    int kmeansIter,
                                                    double hybridFactor) {
        return new DdoModel<>() {
            @Override
            public Problem<MKSState> problem() {
                return problem;
            }

            @Override
            public MKSRelax relaxation() {
                return new MKSRelax();
            }

            @Override
            public MKSRanking ranking() {
                return new MKSRanking();
            }

            @Override
            public WidthHeuristic<MKSState> widthHeuristic() {
                return new FixedWidth<>(maxWidth);
            }

            @Override
            public boolean exportDot() {
                return false;
            }

            @Override
            public StateDistance<MKSState> stateDistance() {
                return new MKSDistance();
            }

            @Override
            public ReductionStrategy<MKSState> relaxStrategy() {
                ReductionStrategy<MKSState> strat = null;
                switch (relaxType) {
                    case Cost -> strat = new CostBased<>(new MKSRanking());
                    case GHP -> strat = new GHP<>(stateDistance(), seed);
                    case Kmeans -> strat = new Kmeans<>(new MKSCoordinates(), kmeansIter, true);
                    case Hybrid -> strat = new Hybrid<>(new MKSRanking(), stateDistance(), hybridFactor, seed);
                    case Random -> strat = new RandomBased<>(seed);
                }
                return strat;
            }

            @Override
            public ReductionStrategy<MKSState> restrictStrategy() {
                ReductionStrategy<MKSState> strat = null;
                switch (restrictType) {
                    case Cost -> strat = new CostBased<>(new MKSRanking());
                    case GHP -> strat = new GHP<>(stateDistance(), seed);
                    case Kmeans -> strat = new Kmeans<>(new MKSCoordinates(), kmeansIter, true);
                    case Hybrid -> strat = new Hybrid<>(new MKSRanking(), stateDistance(), hybridFactor, seed);
                    case Random -> strat = new RandomBased<>(seed);
                }
                return strat;
            }

            @Override
            public DominanceChecker<MKSState> dominance() {
                return new DefaultDominanceChecker<>();
                //return new SimpleDominanceChecker<>(new MKSDominance(), problem.nbVars());
            }

            @Override
            public Frontier<MKSState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.LastExactLayer);
            }

            @Override
            public boolean useCache() {
                return false;
            }
        };
    }

    private static void xpRelaxation() throws IOException {
        MKSProblem[] instances = loadInstances();
        FileWriter writer = new FileWriter("xps/relaxationsMKS.csv");
        writer.write("Instance;Optimal;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;" +
                "isExact;RunTime(ms);Incumbent;NbRelaxations;avgExactNodes;minExactNodes;maxExactNodes;avgMinCardinality;avgMaxCardinality;"+
                "avgAvgCardinality;avgMinDegradation;avgMaxDegradation;avgAvgDegradation\n");

        for (MKSProblem problem : instances) {
            for (int maxWidth = 100; maxWidth <= 1000; maxWidth+=100) {
                for (ClusterType clusterType : new ClusterType[]{ClusterType.Kmeans, ClusterType.Cost, ClusterType.GHP}) {
                    int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5};
                    long[] ghpSeeds = clusterType != ClusterType.GHP ? new long[]{465465} : new long[]{465465, 546351, 87676};
                    double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
                    for (long seed : ghpSeeds) {
                        for (int kmeansIter : kmeansIters) {
                            for (double hybridFactor : hybridFactors) {
                                DdoModel<MKSState> model = getModel(problem,
                                        maxWidth,
                                        clusterType,
                                        seed,
                                        kmeansIter,
                                        hybridFactor);
                                assert problem.name.isPresent();
                                double optimal = problem.optimal.isPresent() ? problem.optimal.get() : -1;
                                System.out.printf("%s %d %d %d %f %n", problem.name.get(), maxWidth, kmeansIter, seed, hybridFactor);
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
        MKSProblem[] instances = loadInstances();
        FileWriter writer = new FileWriter("xps/restrictionMKS.csv");
        writer.write("Instance;Optimal;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;" +
                "isExact;RunTime(ms);Incumbent;NbRestrictions;AvgLayerSize\n");

        for (MKSProblem problem : instances) {
            for (int maxWidth = 100; maxWidth <= 1000; maxWidth+=100) {
                for (ClusterType clusterType : new ClusterType[]{ClusterType.Cost, ClusterType.GHP, ClusterType.Random, ClusterType.Kmeans}) {
                    int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{50};
                    long[] ghpSeeds = (clusterType != ClusterType.GHP) && (clusterType != ClusterType.Random) ? new long[]{465465} : new long[]{465465, 546351, 87676};
                    double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
                    for (long seed : ghpSeeds) {
                        for (int kmeansIter : kmeansIters) {
                            for (double hybridFactor : hybridFactors) {
                                DdoModel<MKSState> model = getModel(problem,
                                        maxWidth,
                                        clusterType,
                                        seed,
                                        kmeansIter,
                                        hybridFactor);
                                assert problem.name.isPresent();
                                double optimal = problem.optimal.isPresent() ? problem.optimal.get() : -1;
                                System.out.printf("%s %s %d %d %d %f %n", problem.name.get(), clusterType, maxWidth, kmeansIter, seed, hybridFactor);
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


    private static void xpBnB() throws IOException {
        MKSProblem[] instances = loadInstances();
        FileWriter writer = new FileWriter("xps/bnBMKS.csv");
        writer.write("Instance;RelaxType;RestrictType;MaxWidth;Seed;KmeansIter;HybridFactor;" +
                "Status;nbIterations;queueMaxSize;RunTimeMs(ms);Incumbent;Gap\n");

        int maxWidth = 60;
        int kmeansIter = -1;
        double hybridFactor = -1;
        ClusterType relaxType = ClusterType.Cost;
        ClusterType[] restrictTypes = new ClusterType[]{ClusterType.Cost, ClusterType.GHP, ClusterType.Random};

        for (MKSProblem problem : instances) {
            for (ClusterType restrictType : restrictTypes) {
                long[] seeds = (restrictType != ClusterType.GHP) && (restrictType != ClusterType.Random) ? new long[]{465465} : new long[]{465465, 546351, 87676};
                for (long seed : seeds) {
                    DdoModel<MKSState> model = getModel(problem,
                            maxWidth,
                            relaxType,
                            restrictType,
                            seed,
                            kmeansIter,
                            hybridFactor);
                    assert problem.name.isPresent();
                    System.out.printf("%s %s %d %d %d %f %n", problem.name.get(), restrictType, maxWidth, kmeansIter, seed, hybridFactor);
                    SearchStatistics stats = Solvers.minimizeDdo(model);

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
            xpRelaxation();
            xpRestriction();
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
