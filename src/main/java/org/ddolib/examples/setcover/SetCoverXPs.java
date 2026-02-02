package org.ddolib.examples.setcover;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.dominance.SimpleDominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.examples.knapsack.KSProblem;
import org.ddolib.examples.maximumcoverage.*;
import org.ddolib.modeling.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static java.lang.Math.ceil;

public class SetCoverXPs {

    public static SetCoverProblem[] loadInstances() throws IOException {
        String instancePath = Path.of("data", "SetCover", "measurements", "or_library", "set_covering").toString();
        System.out.println(instancePath);
        File instanceDir = new File(instancePath);

        File[] files = instanceDir.listFiles(File::isFile);
        assert files != null;
        SetCoverProblem[] problems = new SetCoverProblem[files.length];
        for (int i = 0; i < files.length; i++) {
            problems[i] = new SetCoverProblem(files[i].getAbsolutePath(), false);
        }
        return problems;
    }


    private static DdoModel<SetCoverState> getModel(SetCoverProblem problem,
                                                    int maxWidth,
                                                    ClusterType clusterType,
                                                    long seed,
                                                    int kmeansIter,
                                                    double hybridFactor) {
        return getModel(problem, maxWidth, clusterType, clusterType, seed, kmeansIter, hybridFactor);
    }

    private static DdoModel<SetCoverState> getModel(SetCoverProblem problem,
                                                    int maxWidth,
                                                    ClusterType relaxType,
                                                    ClusterType restrictType,
                                                    long seed,
                                                    int kmeansIter,
                                                    double hybridFactor) {
        return new DdoModel<>() {
            @Override
            public Problem<SetCoverState> problem() {
                return problem;
            }

            @Override
            public Relaxation<SetCoverState> relaxation() {
                return new SetCoverRelax();
            }

            @Override
            public StateRanking<SetCoverState> ranking() {
                return new SetCoverRanking();
            }

            @Override
            public WidthHeuristic<SetCoverState> widthHeuristic() {
                return new FixedWidth<>(maxWidth);
            }

            @Override
            public StateDistance<SetCoverState> stateDistance() {
                return new SetCoverDistance(problem);
            }

            @Override
            public ReductionStrategy<SetCoverState> relaxStrategy() {
                ReductionStrategy<SetCoverState> strat = null;
                switch (relaxType) {
                    case Cost -> strat = new CostBased<>(new SetCoverRanking());
                    case GHP -> strat = new GHP<>(stateDistance(), seed);
                    case Kmeans -> strat = new Kmeans<>(new SetCoverCoordinates(problem), kmeansIter, false);
                    case Hybrid -> strat = new Hybrid<>(new SetCoverRanking(), stateDistance(), hybridFactor, seed);
                    case Random -> strat = new RandomBased<>(seed);
                }
                return strat;
            }

            @Override
            public ReductionStrategy<SetCoverState> restrictStrategy() {
                ReductionStrategy<SetCoverState> strat = null;
                switch (restrictType) {
                    case Cost -> strat = new CostBased<>(new SetCoverRanking());
                    case GHP -> strat = new GHP<>(stateDistance(), seed);
                    case Kmeans -> strat = new Kmeans<>(new SetCoverCoordinates(problem), kmeansIter, false);
                    case Hybrid -> strat = new Hybrid<>(new SetCoverRanking(), stateDistance(), hybridFactor, seed);
                    case Random -> strat = new RandomBased<>(seed);
                }
                return strat;
            }

            @Override
            public DominanceChecker<SetCoverState> dominance() {
                // return new DefaultDominanceChecker<>();
                return new SimpleDominanceChecker<>(new SetCoverDominance(), problem.nbVars());
            }

            @Override
            public Frontier<SetCoverState> frontier() {
                return new SimpleFrontier<>(ranking(), CutSetType.LastExactLayer);
            }

            @Override
            public FastLowerBound<SetCoverState> lowerBound() {
                return new DefaultFastLowerBound<>();
            }
        };
    }

    private static void xpRelaxation() throws IOException {
        SetCoverProblem[] instances = loadInstances();
        FileWriter writer = new FileWriter("xps/relaxationsSetCover.csv");
        writer.write("Instance;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;" +
                "isExact;RunTime(ms);Incumbent;NbRelaxations" +
                ";avgExactNodes;geoAvgExactNodes;minExactNodes;maxExactNodes;varExactNodes" +
                ";avgStateCardinalities;geoAvgStateCardinalities;minStateCardinalities;maxStateCardinalities;varStateCardinalities" +
                ";avgStateDegradation;geoAvgStateDegradation;minStateDegradation;maxStateDegradation;varStateDegradation" +
                ";avgLayerSize;geoAvgLayerSize;minLayerSize;maxLayerSize;varLayerSize;nbNode;nbExactNode" +
                "\n");

        for (SetCoverProblem problem : instances) {
            for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
                for (ClusterType clusterType : new ClusterType[]{ClusterType.Kmeans}) {
                    int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5};
                    long[] ghpSeeds = clusterType != ClusterType.GHP ? new long[]{465465} : new long[]{465465, 546351, 87676};
                    double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.2, 0.4, 0.6, 0.8};
                    for (long seed : ghpSeeds) {
                        for (int kmeansIter : kmeansIters) {
                            for (double hybridFactor : hybridFactors) {
                                DdoModel<SetCoverState> model = getModel(problem,
                                        maxWidth,
                                        clusterType,
                                        seed,
                                        kmeansIter,
                                        hybridFactor);
                                assert problem.name.isPresent();
                                System.out.printf("%s %d %d %d %f %n", problem.name.get(), maxWidth, kmeansIter, seed, hybridFactor);
                                Solution solution = Solvers.relaxedDdo(model);

                                writer.append(String.format("%s;%s;%d;%d;%d;%f;%s%n",
                                        problem.name.get(),
                                        clusterType,
                                        maxWidth,
                                        seed,
                                        kmeansIter,
                                        hybridFactor,
                                        solution
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
        SetCoverProblem[] instances = loadInstances();
        FileWriter writer = new FileWriter("xps/restrictionSetCover.csv");
        writer.write("Instance;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;" +
                "isExact;RunTime(ms);Incumbent;NbRestrictions;AvgLayerSize\n");

        for (SetCoverProblem problem : instances) {
            for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
                for (ClusterType clusterType : new ClusterType[]{ClusterType.Cost, ClusterType.Random, ClusterType.GHP, ClusterType.Hybrid, ClusterType.Kmeans}) {
                    int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5};
                    long[] ghpSeeds = (clusterType != ClusterType.GHP) && (clusterType != ClusterType.Random) ? new long[]{465465} : new long[]{465465, 546351, 87676};
                    double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.2, 0.4, 0.6, 0.8};
                    for (long seed : ghpSeeds) {
                        for (int kmeansIter : kmeansIters) {
                            for (double hybridFactor : hybridFactors) {
                                DdoModel<SetCoverState> model = getModel(problem,
                                        maxWidth,
                                        clusterType,
                                        seed,
                                        kmeansIter,
                                        hybridFactor);
                                assert problem.name.isPresent();
                                System.out.printf("%s %s %d %d %d %f %n", problem.name.get(), clusterType, maxWidth, kmeansIter, seed, hybridFactor);
                                Solution solution = Solvers.restrictedDdo(model);

                                writer.append(String.format("%s;%s;%d;%d;%d;%f;%s%n",
                                        problem.name.get(),
                                        clusterType,
                                        maxWidth,
                                        seed,
                                        kmeansIter,
                                        hybridFactor,
                                        solution
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

    private static void xpRestriction(String instance) throws IOException {
        SetCoverProblem problem = new SetCoverProblem(instance, false);
        String[] nameParts = instance.split("/");
        FileWriter writer = new FileWriter("results_restriction/" + nameParts[nameParts.length - 1] + ".csv");

        for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
            for (ClusterType clusterType : new ClusterType[]{ClusterType.Cost, ClusterType.Random, ClusterType.GHP, ClusterType.Hybrid, ClusterType.Kmeans}) {
                int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5};
                long[] ghpSeeds = (clusterType != ClusterType.GHP) && (clusterType != ClusterType.Random) ? new long[]{465465} : new long[]{465465, 546351, 87676};
                double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.2, 0.4, 0.6, 0.8};
                for (long seed : ghpSeeds) {
                    for (int kmeansIter : kmeansIters) {
                        for (double hybridFactor : hybridFactors) {
                            DdoModel<SetCoverState> model = getModel(problem,
                                    maxWidth,
                                    clusterType,
                                    seed,
                                    kmeansIter,
                                    hybridFactor);
                            assert problem.name.isPresent();
                            double optimal = problem.optimal.isPresent() ? problem.optimal.get() : -1;
                            System.out.printf("%s %f %d %d %d %f %n", problem.name.get(), optimal, maxWidth, kmeansIter, seed, hybridFactor);
                            Solution solution = Solvers.restrictedDdo(model);

                            writer.append(String.format("%s;%f;%s;%d;%d;%d;%f;%f;%d%n",
                                    problem.name.get(),
                                    optimal,
                                    clusterType,
                                    maxWidth,
                                    seed,
                                    kmeansIter,
                                    hybridFactor,
                                    solution.value(),
                                    solution.statistics().runTimeMs()
                            ));
                            writer.flush();
                        }
                    }
                }
            }
        }
        writer.close();
    }

    private static void xpRelaxation(String instance) throws IOException {
        SetCoverProblem problem = new SetCoverProblem(instance, false);
        String[] nameParts = instance.split("/");
        FileWriter writer = new FileWriter("results_relaxation/" + nameParts[nameParts.length - 1] + ".csv");

        for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
            for (ClusterType clusterType : new ClusterType[]{ClusterType.GHP, ClusterType.Kmeans, ClusterType.Hybrid, ClusterType.Cost}) {
                int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5};
                long[] ghpSeeds = clusterType != ClusterType.GHP ? new long[]{465465} : new long[]{465465, 546351, 87676};
                double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.2, 0.4, 0.6, 0.8};
                for (long seed : ghpSeeds) {
                    for (int kmeansIter : kmeansIters) {
                        for (double hybridFactor : hybridFactors) {
                            DdoModel<SetCoverState> model = getModel(problem,
                                    maxWidth,
                                    clusterType,
                                    seed,
                                    kmeansIter,
                                    hybridFactor);
                            assert problem.name.isPresent();
                            double optimal = problem.optimal.isPresent() ? problem.optimal.get() : -1;
                            System.out.printf("%s %d %d %d %f %n", problem.name.get(), maxWidth, kmeansIter, seed, hybridFactor);
                            Solution solution = Solvers.relaxedDdo(model);

                            writer.append(String.format("%s;%f;%s;%d;%d;%d;%f;%f;%d%n",
                                    problem.name.get(),
                                    optimal,
                                    clusterType,
                                    maxWidth,
                                    seed,
                                    kmeansIter,
                                    hybridFactor,
                                    solution.value(),
                                    solution.statistics().runTimeMs()
                            ));
                            writer.flush();
                        }
                    }
                }
            }
        }
        writer.close();
    }


    private static void xpBnB(String instance) throws IOException {
        SetCoverProblem problem = new SetCoverProblem(instance, false);
        String[] nameParts = instance.split("/");
        FileWriter writer = new FileWriter("results/" + nameParts[nameParts.length - 1] + ".csv");

        int maxWidth = 60;
        int kmeansIter = 5;
        double hybridFactor = -1;
        ClusterType[] relaxTypes = {ClusterType.Cost, ClusterType.GHP, ClusterType.Random, ClusterType.Kmeans};
        ClusterType[] restrictTypes = new ClusterType[]{ClusterType.Cost, ClusterType.GHP, ClusterType.Random, ClusterType.Kmeans};
        for (ClusterType relaxType: relaxTypes) {
            for (ClusterType restrictType : restrictTypes) {
                long[] seeds = (restrictType != ClusterType.GHP) && (restrictType != ClusterType.Random) ? new long[]{465465} : new long[]{465465, 546351, 87676};
                for (long seed : seeds) {
                    DdoModel<SetCoverState> model = getModel(problem,
                            maxWidth,
                            relaxType,
                            restrictType,
                            seed,
                            kmeansIter,
                            hybridFactor);
                    assert problem.name.isPresent();
                    double optimal = problem.optimal.isPresent() ? problem.optimal.get() : -1;
                    System.out.printf("%s %s %d %d %d %f %n", problem.name.get(), restrictType, maxWidth, kmeansIter, seed, hybridFactor);
                    long startTime = System.currentTimeMillis();
                    Solution solution = Solvers.minimizeDdo(model, x -> (System.currentTimeMillis() - startTime >= 1000.0*60.0));

                    writer.append(String.format("%s;%s;%s;%d;%d;%d;%f;%s%n",
                            problem.name.get(),
                            relaxType,
                            restrictType,
                            maxWidth,
                            seed,
                            kmeansIter,
                            hybridFactor,
                            solution.statistics().toCSV()
                    ));
                    writer.flush();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            xpRelaxation(args[0]);
            xpRestriction(args[0]);
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
