package org.ddolib.examples.mks;

import org.ddolib.common.dominance.DefaultDominanceChecker;
import org.ddolib.common.dominance.DominanceChecker;
import org.ddolib.common.solver.Solution;
import org.ddolib.ddo.core.frontier.CutSetType;
import org.ddolib.ddo.core.frontier.Frontier;
import org.ddolib.ddo.core.frontier.SimpleFrontier;
import org.ddolib.ddo.core.heuristics.cluster.*;
import org.ddolib.ddo.core.heuristics.width.FixedWidth;
import org.ddolib.ddo.core.heuristics.width.WidthHeuristic;
import org.ddolib.modeling.DdoModel;
import org.ddolib.modeling.Problem;
import org.ddolib.modeling.Solvers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

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
                return new MKSDistance(problem);
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
        writer.write("Instance;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;" +
                "isExact;RunTime(ms);Incumbent;NbRelaxations" +
                ";avgExactNodes;geoAvgExactNodes;minExactNodes;maxExactNodes;varExactNodes" +
                ";avgStateCardinalities;geoAvgStateCardinalities;minStateCardinalities;maxStateCardinalities;varStateCardinalities" +
                ";avgStateDegradation;geoAvgStateDegradation;minStateDegradation;maxStateDegradation;varStateDegradation" +
                ";avgLayerSize;geoAvgLayerSize;minLayerSize;maxLayerSize;varLayerSize;nbNode;nbExactNode" +
                ";\n");

        for (MKSProblem problem : instances) {
            for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
                for (ClusterType clusterType : new ClusterType[]{ClusterType.Hybrid, ClusterType.GHP, ClusterType.Cost, ClusterType.Kmeans}) {
                    int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5};
                    long[] ghpSeeds = clusterType != ClusterType.GHP ? new long[]{465465} : new long[]{465465, 546351, 87676};
                    double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.2, 0.4, 0.6, 0.8};
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
                                Solution solution = Solvers.relaxedDdo(model);

                                writer.append(String.format("%s;%f;%s;%d;%d;%d;%f;%s%n",
                                        problem.name.get(),
                                        optimal,
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
        MKSProblem[] instances = loadInstances();
        FileWriter writer = new FileWriter("xps/restrictionMKS.csv");
        writer.write("Instance;Optimal;ClusterStrat;MaxWidth;Seed;KmeansIter;HybridFactor;" +
                "isExact;RunTime(ms);Incumbent;NbRestrictions;AvgLayerSize\n");

        for (MKSProblem problem : instances) {
            for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
                for (ClusterType clusterType : new ClusterType[]{ClusterType.Cost, ClusterType.GHP, ClusterType.Random, ClusterType.Kmeans, ClusterType.Hybrid}) {
                    int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5};
                    long[] ghpSeeds = (clusterType != ClusterType.GHP) && (clusterType != ClusterType.Random) ? new long[]{465465} : new long[]{465465, 546351, 87676};
                    double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.2, 0.4, 0.6, 0.8};
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
                                Solution solution = Solvers.restrictedDdo(model);

                                writer.append(String.format("%s;%f;%s;%d;%d;%d;%f;%s%n",
                                        problem.name.get(),
                                        optimal,
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

    private static void xpRelaxation(String instance) throws IOException {
        MKSProblem problem = new MKSProblem(instance);
        String[] nameParts = instance.split("/");
        FileWriter writer = new FileWriter("results_relaxation" + nameParts[nameParts.length - 1].replace(".txt", ".csv"));

        for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
            for (ClusterType clusterType : new ClusterType[]{ClusterType.Hybrid, ClusterType.GHP, ClusterType.Cost, ClusterType.Kmeans}) {
                int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5};
                long[] ghpSeeds = clusterType != ClusterType.GHP ? new long[]{465465} : new long[]{465465, 546351, 87676};
                double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.2, 0.4, 0.6, 0.8};
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
                            Solution solution = Solvers.relaxedDdo(model);

                            writer.append(String.format("%s;%f;%s;%d;%d;%d;%f;%s%n",
                                    problem.name.get(),
                                    optimal,
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
        writer.close();
    }

    private static void xpRestriction(String instance) throws IOException {
        MKSProblem problem = new MKSProblem(instance);
        String[] nameParts = instance.split("/");
        FileWriter writer = new FileWriter("results_restriction" + nameParts[nameParts.length - 1].replace(".txt", ".csv"));

        for (int maxWidth = 10; maxWidth <= 100; maxWidth+=10) {
            for (ClusterType clusterType : new ClusterType[]{ClusterType.Cost, ClusterType.GHP, ClusterType.Random, ClusterType.Kmeans, ClusterType.Hybrid}) {
                int[] kmeansIters = clusterType != ClusterType.Kmeans ? new int[]{-1} : new int[]{5};
                long[] ghpSeeds = (clusterType != ClusterType.GHP) && (clusterType != ClusterType.Random) ? new long[]{465465} : new long[]{465465, 546351, 87676};
                double[] hybridFactors = clusterType != ClusterType.Hybrid ? new double[]{-1} : new double[] {0.2, 0.4, 0.6, 0.8};
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
                            Solution solution = Solvers.restrictedDdo(model);

                            writer.append(String.format("%s;%f;%s;%d;%d;%d;%f;%s%n",
                                    problem.name.get(),
                                    optimal,
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
        writer.close();
    }

    private static void xpBnB(String instance) throws IOException {
        MKSProblem problem = new MKSProblem(instance);
        String[] nameParts = instance.split("/");
        FileWriter writer = new FileWriter("results/" + nameParts[nameParts.length - 1].replace(".txt", ".csv"));
        // writer.write("Instance;RelaxType;RestrictType;MaxWidth;Seed;KmeansIter;HybridFactor;" +
        //        "Status;nbIterations;queueMaxSize;RunTimeMs(ms);Incumbent;Gap\n");

        int maxWidth = 60;
        //  int kmeansIter = -1;
        double hybridFactor = -1;
        ClusterType[] relaxTypes = new ClusterType[]{ClusterType.Cost, ClusterType.GHP, ClusterType.Kmeans};
        ClusterType[] restrictTypes = new ClusterType[]{ClusterType.Cost, ClusterType.GHP, ClusterType.Random, ClusterType.Kmeans};
        for (ClusterType relaxType: relaxTypes) {
            for (ClusterType restrictType : restrictTypes) {
                int[] kmeansIters = (relaxType != ClusterType.Kmeans && restrictType != ClusterType.Kmeans ) ? new int[]{-1} : new int[]{5};
                long[] seeds = (relaxType != ClusterType.GHP && restrictType != ClusterType.GHP && restrictType != ClusterType.Random) ? new long[]{465465} : new long[]{465465, 546351, 87676};
                for (long seed : seeds) {
                    for (int kmeansIter : kmeansIters) {
                        DdoModel<MKSState> model = getModel(problem,
                                maxWidth,
                                relaxType,
                                restrictType,
                                seed,
                                kmeansIter,
                                hybridFactor);
                        assert problem.name.isPresent();
                        System.out.printf("%s %s %d %d %d %f %n", problem.name.get(), restrictType, maxWidth, kmeansIter, seed, hybridFactor);
                        long startTime = System.currentTimeMillis();
                        Solution solution = Solvers.minimizeDdo(model, x -> (System.currentTimeMillis() - startTime >= 1000.0 * 300.0));

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
    }

    public static void main(String[] args) {
        try {
            // xpBnB(args[0]);
            xpRelaxation(args[0]);
            xpRestriction(args[0]);
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
